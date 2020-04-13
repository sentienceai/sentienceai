<#-- @ftlvariable name="" type="com.airobotics.commandcenter.views.FootPrintView" -->
<!DOCTYPE html>
<html>
  <body ng-app="myApp" ng-controller="myCtrl">
    <canvas id="myCanvas" width="800" height="1150" style="border:1px solid #d3d3d3;" ng-click="get()">
      Your browser does not support the HTML5 canvas tag.</canvas>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js">
    </script>
    <script>
      var points = [];
      var c = document.getElementById("myCanvas");
      var ctx = c.getContext("2d");
      ctx.transform(1, 0, 0, -1, 0, c.height);
      ctx.beginPath();
      ctx.moveTo(${beacons?first.getX()?string("#")}, ${beacons?first.getY()?string("#")});
      <#list beacons as beacon>
        ctx.lineTo(${beacon.getX()?string("#")}, ${beacon.getY()?string("#")});
        points.push({x:${beacon.getX()?string("#")}, y:${beacon.getY()?string("#")}});
      </#list>
      ctx.stroke();
      ctx.beginPath();
      ctx.moveTo(${route?first.getX()?string("#")}, ${route?first.getY()?string("#")});
      <#list route as routePoint>
        ctx.lineTo(${routePoint.getX()?string("#")}, ${routePoint.getY()?string("#")});
        points.push({x:${routePoint.getX()?string("#")}, y:${routePoint.getY()?string("#")}});
      </#list>
      ctx.strokeStyle = '#00ff00';
      ctx.stroke();
      
      var app = angular.module('myApp', []);
      app.controller('myCtrl', function($scope, $http, $timeout) {
          $scope.getData = function(){
            $http.get('rawdata?sort=-1&limit=2')
              .success(function(data, status, headers, config) {
                ctx.beginPath();
                ctx.moveTo(data[0].location.x, data[0].location.y);
                for (i = 0; i < data.length; i++) {
                  ctx.lineTo(data[i].location.x, data[i].location.y);
                  points.push({x:data[i].location.x, y:data[i].location.y});
                }
                ctx.strokeStyle = '#ff0000';
                ctx.stroke();
                for (i = 0; i < points.length; i++) {
                  ctx.fillText(points[i].x + ', ' + points[i].y + '(' + i + ')', points[i].x, points[i].y);
                }
            });
          };
          $scope.get = function(){
            alert('clicked');
          };
          $scope.getData();
      });
    </script>
  </body>
</html>