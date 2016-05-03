angular.module('plotly', [
])

.directive('plotly', function($window) {
    return {
      restrict: 'E',
      template: '<div></div>',
      scope: {
        data: '=',
        layout: '=',
        options: '=',
        bindings: '='
      },
      link: function(scope, element) {
        var graph = element[0].children[0];
        
        var initialized = false;
        function onUpdate() {
          //No data yet, or clearing out old data
          if (!(scope.data)) {
            if (initialized) {
              Plotly.Plots.purge(graph);
              graph.innerHTML = '';
            }
            return;
          }
          //If this is the first run with data, initialize
          if (!initialized) {
            initialized = true;
            Plotly.newPlot(graph, scope.data, scope.layout, scope.options);
          }
          graph.layout = scope.layout;
          graph.data = scope.data;
          graph.removeAllListeners();
          for (var binding in scope.bindings) {
            if (scope.bindings.hasOwnProperty(binding)) {
              graph.on(binding, scope.bindings[binding]);
            }
          }
          Plotly.redraw(graph);
          Plotly.Plots.resize(graph);
        }

        function onResize() {
          if (!(initialized && scope.data)) {
            return;
          }
          Plotly.Plots.resize(graph);
        }

        scope.$watch(function() {
          return [scope.layout, scope.data];
        }, function() {
          onUpdate();
        }, true);
        
        scope.$watch(function() {
          return {
            'h': element[0].offsetHeight,
            'w': element[0].offsetWidth
          };
        }, function(newValue, oldValue) {
          if (angular.equals(newValue, oldValue)) {
            return;
          }
          onResize();
        }, true);

        angular.element($window).bind('resize', function() {
          onResize();
        });
      }
    };
  }
)

;