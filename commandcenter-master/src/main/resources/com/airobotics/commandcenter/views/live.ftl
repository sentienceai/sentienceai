<#-- @ftlvariable name="" type="com.airobotics.commandcenter.views.FootPrintView" -->
<!DOCTYPE html>
<html>
  <#include "head.ftl">
  <body ng-app="myApp">
    <div id="floating-panel" ng-controller="myCtrl">
      <button ng-click="deactivateAllSchedules()">Deactivate Route</button>
      <button ng-click="clearRoute()">Clear Route</button>
      <select ng-change="setPath()" ng-model="selectedItem">
        <option value="">None</option>
        <option value="T">Triangle</option>
        <option value="R">Rectangle</option>
        <option value="S">Star</option>
      </select>
      <button ng-click="setRoute()">Set Route</button>
      <button ng-click="runRoute()">Run Route</button>
    </div>
    <#include "map.ftl">
    <script>
      var app = angular.module('myApp', []);
      app.controller('myCtrl', function($scope, $interval, $http, $timeout) {
          var interval;
          $scope.getData = function(){
            $http.get('locations/current')
              .success(function(data, status, headers, config) {
                for (i = 0; i < data.length; i++) {
                  footprint.getPath().push(new google.maps.LatLng(data[i].y, data[i].x));
                }
            });
          };
          $scope.setPath = function(){
            var path = poly.getPath();
            while (path.pop()) ;
            switch ($scope.selectedItem) {
              case "T":
                path.push(new google.maps.LatLng(42.32377671, -71.35268110000001));
                path.push(new google.maps.LatLng(42.32377533, -71.35268110000001));
                path.push(new google.maps.LatLng(42.32377783, -71.35268210000001));
                path.push(new google.maps.LatLng(42.32377783, -71.35268010000001));
                path.push(new google.maps.LatLng(42.32377533, -71.35268110000001));
                break;
              case "R":
                path.push(new google.maps.LatLng(42.32377671, -71.35268110000001));
                path.push(new google.maps.LatLng(42.32377533, -71.35268110000001));
                path.push(new google.maps.LatLng(42.32377533, -71.35268210000001));
                path.push(new google.maps.LatLng(42.32377783, -71.35268210000001));
                path.push(new google.maps.LatLng(42.32377783, -71.35268010000001));
                path.push(new google.maps.LatLng(42.32377533, -71.35268010000001));
                path.push(new google.maps.LatLng(42.32377533, -71.35268110000001));
                break;
              case "S":
                path.push(new google.maps.LatLng(42.32377671, -71.35268110000001));
                path.push(new google.maps.LatLng(42.32377533, -71.35268110000001));
                path.push(new google.maps.LatLng(42.32377783, -71.35268210000001));
                path.push(new google.maps.LatLng(42.32377783, -71.35268010000001));
                path.push(new google.maps.LatLng(42.32377533, -71.35268110000001));
                break;
              default:
                break;
            }
          };
          $scope.clearRoute = function(){
            poly.setMap(null);
            initRoute();
            $http.delete('/api/v1/robots/${id}/instructions/cache')
              .success(function(data, status, headers, config) {
                alert("cleared");
            });
          };
          $scope.runRoute = function(){
            alert("start running!");
            $interval.cancel(interval);
          };
          $scope.activateSchedule = function(scheduleId){
            $http.put('/api/v1/schedules/' + scheduleId + '/active')
              .success(function(data, status, headers, config) {
                interval = $interval(clearCacheOnly, 3000);
                alert("activated");
            });
          };
          $scope.deactivateSchedule = function(scheduleId){
            $http.put('/api/v1/schedules/' + scheduleId + '/inactive')
              .success(function(data, status, headers, config) {
                alert("deactivated");
            });
          };
          $scope.updateRoute = function(scheduleId){
            $http.put('/api/v1/robots/${id}/schedules/' + scheduleId + '/routes',
              JSON.stringify(poly.getPath().getArray()).replace(/lat/g, "y").replace(/lng/g, "x"))
              .success(function(data, status, headers, config) {
                $scope.activateSchedule(scheduleId);
            });
          };
          $scope.clearCache = function(scheduleId){
            $http.delete('/api/v1/robots/${id}/instructions/cache?scheduleId=' + scheduleId)
              .success(function(data, status, headers, config) {
                $scope.updateRoute(scheduleId);
            });
          };
          var clearCacheOnly = function(){
            $http.delete('/api/v1/robots/${id}/instructions/cache')
              .success(function(data, status, headers, config) {
            });
          };
          $scope.setRoute = function(){
            $http.get('/api/v1/robots/${id}/schedules')
              .success(function(data, status, headers, config) {
                var scheduleId = 0;
                for (i = 0; i < data.length; i++)
                  scheduleId = data[i].id;
                $scope.clearCache(scheduleId);
            });
          };
          $scope.deactivateAllSchedules = function(){
            $http.get('/api/v1/robots/${id}/schedules')
              .success(function(data, status, headers, config) {
                for (i = 0; i < data.length; i++) {
                  scheduleId = data[i].id;
                  $scope.deactivateSchedule(scheduleId);
                }
            });
          };
          $scope.interval = function(){
            $timeout(function() {
              $scope.getData();
              $scope.interval();
            }, 1000)
          };
          $scope.interval();
      });
    </script>
  </body>
</html>