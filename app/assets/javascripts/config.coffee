init = (requirejs) ->
  'use strict'
  requirejs.config
    shim:{}

  require ['AngularApp', 'app'], (ngApp) ->
    console.log('initializing..')
    ngApp.init()
    console.log('initialized!')

init(requirejs)