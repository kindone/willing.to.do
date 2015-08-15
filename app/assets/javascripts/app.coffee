define ['./TaskManager', 'DefaultTasks','AngularApp'], (TaskManager, Tasks, ngApp) ->

  taskManager = new TaskManager(Tasks)

  labels = []

  scopeTask = taskManager.findById(2)

  getAncestry = (task) ->
      ancestry = []
      parent = if typeof(task.parent) != 'undefined' then taskManager.findById(task.parent) else task.parent
      # traverse up recursively
      while typeof(parent) != 'undefined' && parent.id != 0
          ancestry.unshift parent
          parent = if typeof(parent.parent) != 'undefined' then taskManager.findById(parent.parent) else parent.parent

      ancestry.push(task) # ancestry includes self
      ancestry

  buildTree = (task) ->
      taskTree = {id: task.id, name: task.name, parent: task.parent, children: []}

      taskTree.children = _(task.children).map (childId) ->
          buildTree(taskManager.findById(childId))

      taskTree

  # for displaying projects
  buildLeaflessTree = (task) ->
      taskTree = {id: task.id, name: task.name, parent: task.parent, children: []}
      taskTree.children = _(_(task.children).map((childId) ->
          if taskManager.findById(childId).children
              buildLeaflessTree(taskManager.findById(childId))
          else
              null
      )).compact()
      taskTree


  app = ngApp

  app.controller "Init", ['$scope', '$rootScope', ($scope, $rootScope) ->
      # update base array
      $rootScope.$on 'taskChange', (event, flag) ->
          taskManager.rebuildIdTable()
          console.log 'taskChange'

      $rootScope.$on 'openNewTaskForm', (event, flag) ->
          console.log 'openNewTaskForm'

      $rootScope.$on 'scopeChange', (event, flag) ->
          console.log 'scopeChange'
  ]

  app.controller "ViewCtrl", ($scope) ->
      $scope.inbox = {count:5}
      $scope.priority = {count:1}
      $scope.willing = {count:1}

  app.controller "ProjectCtrl", ['$scope', '$rootScope', ($scope, $rootScope) ->
      buildProjects = () ->
          _(taskManager.getRootLevelTasks()).map (task) ->
              buildLeaflessTree(task)

      $scope.isCurrentProject = ((project) -> project.id == scopeTask.id)

      $scope.projects = buildProjects()

      $scope.setCurrentProject = (taskid) ->
          task = taskManager.findById(taskid)
          scopeTask = task
          $rootScope.$emit 'scopeChange', task

      $rootScope.$on 'taskChange', (event, flag) ->
          console.log 'updating projects', taskManager.tasks
          $scope.projects = buildProjects()
  ]

  app.controller "LabelCtrl", ($scope) ->
      $scope.labels = labels

  app.controller "TaskForm", ['$scope', '$rootScope', ($scope, $rootScope) ->
      $scope.create = ->
          createTask $scope.name, $scope.deadline, $scope.parentId
          $rootScope.$emit 'taskChange'

      $scope.init = (parentId) ->
          $scope.name = ""
          $scope.deadline = Date.now().toString()
          $scope.parentId = parentId
          console.log parentId

      $rootScope.$on 'openNewTaskForm', (event) ->
          $scope.init(scopeTask.id)
  ]

  app.controller "TaskCtrl", ['$scope', '$rootScope', ($scope, $rootScope) ->
      $scope.addTaskEnabled = true
      $scope.treeOptions = {
          dropped: (event) ->
              # only when node dropped to non-self
              if event.source.nodesScope != event.dest.nodesScope or event.source.index != event.dest.index
                  id = event.source.nodeScope.$modelValue.id
                  parentId = if event.source.nodesScope.$nodeScope then event.source.nodesScope.$nodeScope.$modelValue.id else null
                  newParentId = if event.dest.nodesScope.$nodeScope then event.dest.nodesScope.$nodeScope.$modelValue.id else null
                  index = event.source.index
                  newIndex = event.dest.index
                  console.log "dropped: ",
                      "id:", id,
                      "parentId:", parentId,
                      "newParentId:", newParentId,
                      "index:", event.source.index,
                      "newIndex:", event.dest.index

                  if parentId == null
                      console.log "null parent:", parentId, scopeTask.id
                      parentId = scopeTask.id

                  if newParentId == null
                      console.log "null new parent:", newParentId, scopeTask.id
                      newParentId = scopeTask.id

                  if parentId == newParentId
                      taskManager.reorder id, parentId, index, newIndex
                  else
                      taskManager.move id, parentId, index, newParentId, newIndex

                  $rootScope.$emit 'taskChange', true
              #console.log "tasks updated: ", tasks
      }

      $scope.setCurrentScope = (task) ->
          $scope.breadcrumb = getAncestry(task)
          $scope.tasks = _(task.children).map((childId) ->
              buildTree(taskManager.findById(childId))
         )


      $scope.toggleChildren = (scope) ->
          console.log scope
          scope.collapsed = if scope.collapsed then false else true


      $scope.setCurrentScope(scopeTask)

      $scope.openNewTaskForm = () ->
          $rootScope.$emit 'openNewTaskForm'

      $scope.createInplaceTaskForm = () ->
         # place <FORM> node into the tree
         $scope.formTask = taskManager.findById(-1)
         $scope.formTask.tempName = ""
         scopeTask.children.push(-1)
         $scope.formTask.parent = scopeTask.id
         # emit taskchange
         $rootScope.$emit('taskChange')
         $scope.addTaskEnabled = false

         setTimeout( ->
              $('#task-inplace-form').focus()
              console.log('GO')
          ,100)

      $scope.confirmCreateNewTask = ->
          formTask = taskManager.findById(-1)
          parentTask = taskManager.findById(formTask.parent)
          position = _(parentTask.children).indexOf(-1)
          # remove form task in the children list first
          parentTask.children = _(parentTask.children).without(-1)
          # next it will take care of the position
          taskManager.create(formTask.tempName, formTask.deadline, formTask.parent, position)

          $rootScope.$emit 'taskChange'
          $scope.addTaskEnabled = true

      $scope.deleteTask = (taskId) ->
          taskManager.delete(taskId)
          $rootScope.$emit 'taskChange'

      # event handlers
      $rootScope.$on 'scopeChange', (event, task) ->
          $scope.setCurrentScope(task)

      $rootScope.$on 'taskChange', (event, flag) ->
          if !flag
              if scopeTask.children
                  $scope.tasks = _(scopeTask.children).map (childId) ->
                      buildTree(taskManager.findById(childId))

  ]

  app
