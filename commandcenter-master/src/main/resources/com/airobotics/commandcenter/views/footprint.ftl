<#-- @ftlvariable name="" type="com.airobotics.commandcenter.views.FootPrintView" -->
<!DOCTYPE html>
<html>
  <#include "head.ftl">
  <body>
    <#include "map.ftl">
    <script>
      function push(data, i) {
        if (data[i])
          footprint.getPath().push(new google.maps.LatLng(data[i].y, data[i].x));
        if (i < data.length){
          setTimeout(function(){
                i++;
                push(data, i);
          }, 100);
        }
      }
      var app = angular.module('myApp', []);
      app.controller('myCtrl', function($scope, $http) {
          $http.get("${id}/locations")
          .then(function(response) {
              push(response.data, 0);
          });
      });
    </script>
  </body>
</html>