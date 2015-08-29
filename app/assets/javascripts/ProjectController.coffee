define [], () ->

  class ProjectController
    constructor: (ngApp, taskManager, taskUtils)->

      ngApp.controller "ProjectController", ['$scope', '$rootScope', ($scope, $rootScope) ->
        buildProjects = () ->
          _(taskManager.getRootLevelTasks()).map (task) ->
            taskUtils.buildTaskTreeLeafless(task)

        $scope.isCurrentProject = ((project) -> project.id == $rootScope.scopeTask.id)

        $scope.projects = buildProjects()

        $scope.setCurrentProject = (taskid) ->
          task = taskManager.findById(taskid)
          $rootScope.scopeTask = task
          $rootScope.$emit 'scopeChange', task

        $rootScope.$on 'taskChange', (event, flag) ->
          console.log 'updating projects', taskManager.tasks
          $scope.projects = buildProjects()
      ]