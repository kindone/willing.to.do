define [], () ->

  class TaskManager

    constructor:(@tasks) ->
      # add pseudo tasks
      formTask = {id: @formTaskId, name: "<FORM>"}
      @tasks.unshift(formTask)
      @tasksById = @genTasksByIdTable(@tasks)

    genTasksByIdTable: (tasks) ->
      byId = {}
      _(tasks).each (task) ->
        byId[task.id] = task
      byId

    formTaskId: -1

    inboxId: -2

    rootId: 0

    getFormTask: () ->
      @tasksById[@formTaskId]

    getRoot: () ->
      @tasksById[@rootId]

    getInbox: () ->
      @tasksById[@inboxId]

    rebuildIdTable: ->
      @tasksById = @genTasksByIdTable(@tasks)

    findById: (id) ->
      @tasksById[id]

    create : (name, deadline, parentId, position = -1) ->
      newId = (_(@tasks).max (task) ->
        task.id
      ).id + 1

      newTask = {id: newId, name: name, parent: parentId, deadline: deadline}
      @tasks.push newTask

      # TODO: want to insert at specific position
      if parentId
        if typeof(@tasksById[parentId].children) == 'undefined'
          @tasksById[parentId].children = [newId]
        else
          if position >= 0
            @tasksById[parentId].children.splice position, 0, newId
          else
            @tasksById[parentId].children.push(newId)

    reorder: (id, parentId, index, newIndex) ->
      if id < 0
        return

      if !parentId
        parentId = 0
      arr = @tasksById[parentId].children
      target = @tasksById[parentId].children[index]
      arr.splice(index, 1) # remove at old index
      arr.splice(newIndex, 0, target) # insert at new index

    move: (id, parentId, index, newParentId, newIndex) ->
      if id < 0
        return
      arr = @tasksById[parentId].children
      newArr = @tasksById[newParentId].children
      target = @tasksById[parentId].children[index]
      # set new parent
      @tasksById[target].parent = newParentId

      arr.splice index, 1 # remove at old index
      if arr.length == 0
        delete @tasksById[parentId].children

      if newArr
        newArr.splice newIndex, 0, target # insert at new index
      else
        @tasksById[newParentId].children = [target] # create new children array


    delete: (id) ->
      if id < 0
        return
      deleteTree = (id) =>
        task = @tasksById[id]
        if task.children
          _(task.children).each (childId)->
            deleteTree(childId)
        if @tasksById[id].parent > 0 # delete single task
          parent = @tasksById[@tasksById[id].parent]
          parent.children = _(parent.children).without id

        @tasks = _(@tasks).reject (task) -> task.id == id

      deleteTree(id)

    getRootLevelTasks:  ->
      _(@tasks).filter (task)->
        task.parent == 0
