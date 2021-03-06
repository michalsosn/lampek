angular.module('lampek.defaults', [
])

.factory('fillMatrix', function() {
  return function(rows, cols, val) {
    if (cols === undefined) {
      cols = rows;
    }
    if (val === undefined) {
      val = 0;
    }
    
    var array = [], row = [];
    while (cols--) {
      row.push(val);
    }
    while (rows--) {
      array.push(row.slice());
    }
    return array;
  };
})

.factory('fillDefaults', function(fillMatrix) {
  return function(spec) {
    var returned = { };
    for (var param in spec.parameters) {
      if (spec.parameters.hasOwnProperty(param)) {
        switch (spec.parameters[param].type) {
          case 'INTEGER':
            if (spec.parameters[param].def !== undefined) {
              returned[param] = spec.parameters[param].def;
            } else {
              returned[param] = Math.round((spec.parameters[param].min + spec.parameters[param].max) / 2);
            }
            break;
          case 'DOUBLE':
            if (spec.parameters[param].def !== undefined) {
              returned[param] = spec.parameters[param].def;
            } else {
              returned[param] = (spec.parameters[param].min + spec.parameters[param].max) / 2;
            }
            break;
          case 'ENUM':
            returned[param] = spec.parameters[param].values[0];
            break;
          case 'MATRIX':
            returned[param] = fillMatrix(3);
            break;
          case 'IMAGE':
            returned[param] = "";
            break;
        }
      }
    }
    return returned;
  };
})

;
