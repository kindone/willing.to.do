define ['./TaskManager', './TaskUtils', './DefaultTasks',
        './ViewController',
        './TaskController',
        './TaskFormController',
        './LabelController',
        './ProjectController'], (TaskManager, TaskUtils, DefaultTasks,
   ViewController, TaskController, TaskFormController, LabelController, ProjectController) ->

  ngApp = angular.module "App", ['ui.tree']
  taskManager = new TaskManager(DefaultTasks)
  taskUtils = new TaskUtils(taskManager)

  new TaskController(ngApp, taskManager, taskUtils)
  new ViewController(ngApp, taskManager, taskUtils)
  new TaskFormController(ngApp, taskManager, taskUtils)
  new LabelController(ngApp, taskManager, taskUtils)
  new ProjectController(ngApp, taskManager, taskUtils)


  # initialize angular module
  ngApp.run ['$rootScope', ($rootScope) ->
    # update Tasks
    $rootScope.scopeTask = taskManager.findById(2)
    $rootScope.$on 'taskChange', (event, flag) ->
      taskManager.rebuildIdTable()
      console.log 'taskChange'

    $rootScope.$on 'openNewTaskForm', (event, flag) ->
      console.log 'openNewTaskForm'

    $rootScope.$on 'scopeChange', (event, flag) ->
      console.log 'scopeChange'
  ]

  ngApp.init = ->
    angular.bootstrap(document, ['App'])

  ngApp
