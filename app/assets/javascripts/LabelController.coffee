define [], () ->

  class LabelController
    constructor: (ngApp, taskManager, taskUtils) ->
      ngApp.controller "LabelCtrl", ($scope) ->
        # todo: fill with actual labels
        $scope.labels = []