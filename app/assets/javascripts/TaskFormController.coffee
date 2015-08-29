define [], ()->
  class TaskFormController

    constructor: (ngApp, taskManager, taskUtils) ->
      ngApp.controller "TaskFormController", ['$scope', '$rootScope', ($scope, $rootScope) ->
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