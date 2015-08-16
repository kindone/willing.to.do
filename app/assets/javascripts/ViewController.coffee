define [], () ->

  class ViewController

    constructor: (ngApp, taskManager, taskUtils) ->
      ngApp.controller "ViewCtrl", ($scope) ->
        $scope.inbox = {count:5}
        $scope.priority = {count:1}
        $scope.willing = {count:1}