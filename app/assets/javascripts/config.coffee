init = (requirejs) ->
  'use strict'
  requirejs.config
    shim:{}

  require ['main'], (app) ->
    app.init()

init(requirejs)