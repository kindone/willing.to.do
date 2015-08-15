define [], () ->
  app = angular.module "App", ['ui.tree']

  app.init = ->
    angular.bootstrap(document, ['App'])

  app