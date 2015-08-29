define [], () ->

  class LabelController
    constructor: (ngApp, taskManager, taskUtils) ->
      ngApp.controller "LabelController", ($scope) ->
        # todo: fill with actual labels
        $scope.labels = []