define [], () ->

  class TaskController

    constructor: (ngApp, taskManager, taskUtils) ->
      ngApp.controller "TaskController", ['$scope', '$rootScope', ($scope, $rootScope) ->
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

              unless parentId?
                console.log "null parent:", parentId, $rootScope.scopeTask.id
                parentId = $rootScope.scopeTask.id

              unless newParentId?
                console.log "null new parent:", newParentId, $rootScope.scopeTask.id
                newParentId = $rootScope.scopeTask.id

              if parentId == newParentId
                taskManager.reorder id, parentId, index, newIndex
              else
                taskManager.move id, parentId, index, newParentId, newIndex

              $rootScope.$emit 'taskChange', true
              #console.log "tasks updated: ", tasks
        }

        $scope.setCurrentProject = (taskid) ->
          task = taskManager.findById(taskid)
          $rootScope.scopeTask = task
          $rootScope.$emit 'scopeChange', task

        $scope.setCurrentScope = (task) ->
          $scope.breadcrumb = taskUtils.buildTaskAncestry(task)
          $scope.tasks = _(task.children).map((childId) ->
            taskUtils.buildTaskTree(taskManager.findById(childId))
          )

        $scope.toggleChildren = (scope) ->
          console.log scope
          scope.collapsed = not scope.collapsed

        $scope.setCurrentScope($rootScope.scopeTask)

        $scope.openNewTaskForm = ->
          $rootScope.$emit 'openNewTaskForm'

        $scope.createInplaceTaskForm = ->
          # place <FORM> node into the tree
          $scope.formTask = taskManager.getFormTask()
          $scope.formTask.tempName = ""
          $rootScope.scopeTask.children.push(taskManager.formTaskId)
          $scope.formTask.parent = $rootScope.scopeTask.id
          # emit taskchange
          $rootScope.$emit('taskChange')
          $scope.addTaskEnabled = false

          setTimeout( ->
            $('#task-inplace-form').focus()
            console.log('GO')
          ,100)

        $scope.confirmCreateNewTask = ->
          formTask = taskManager.getFormTask()
          parentTask = taskManager.findById(formTask.parent)
          position = _(parentTask.children).indexOf(taskManager.formTaskId)
          # remove form task in the children list first
          parentTask.children = _(parentTask.children).without(taskManager.formTaskId)
          # next it will take care of the position
          taskManager.create(formTask.tempName, formTask.deadline, formTask.parent, position)

          $rootScope.$emit 'taskChange'
          $scope.addTaskEnabled = true

        $scope.cancelCreateNewTask = ->
          formTask = taskManager.getFormTask()
          parentTask = taskManager.findById(formTask.parent)
          # remove form task in the children list
          parentTask.children = _(parentTask.children).without(taskManager.formTaskId)

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
            if $rootScope.scopeTask.children
              $scope.tasks = _($rootScope.scopeTask.children).map (childId) ->
                taskUtils.buildTaskTree(taskManager.findById(childId))

      ]