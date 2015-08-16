define [], () ->

  class TaskUtils

    constructor: (taskManager) ->
      @taskManager = taskManager

    buildTaskAncestry: (task) ->
      ancestry = []
      parent = if typeof(task.parent) != 'undefined' then @taskManager.findById(task.parent) else task.parent
      # traverse up recursively
      while typeof(parent) != 'undefined' && parent.id != 0
        ancestry.unshift parent
        parent = if typeof(parent.parent) != 'undefined' then @taskManager.findById(parent.parent) else parent.parent

      ancestry.push(task) # ancestry includes self
      ancestry

    buildTaskTree: (task) ->
      taskTree = {id: task.id, name: task.name, parent: task.parent, children: []}

      taskTree.children = _(task.children).map (childId) =>
        @buildTaskTree(@taskManager.findById(childId))

      taskTree

    # for displaying projects
    buildTaskTreeLeafless: (task) ->
      taskTree = {id: task.id, name: task.name, parent: task.parent, children: []}
      taskTree.children = _(_(task.children).map((childId) =>
        if @taskManager.findById(childId).children
          @buildTaskTreeLeafless(@taskManager.findById(childId))
        else
          null
      )).compact()
      taskTree