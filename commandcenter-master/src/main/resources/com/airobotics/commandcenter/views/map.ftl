    <div id="map" ng-app="myApp" ng-controller="myCtrl"></div>
    <script>
      var map;
      var poly;
      var footprint;

      function initMap() {
        map = new google.maps.Map(document.getElementById('map'), {
          zoom: 25,
          center: {lat: ${centroid.getY()?string("###.########")}, lng: ${centroid.getX()?string("###.########")}}
        });

        // Define the LatLng coordinates for the polygon's path.
        var triangleCoords = [
          <#list boundaryPoints as boundaryPoint>
		    {lat: ${boundaryPoint.getY()?string("###.########")}, lng: ${boundaryPoint.getX()?string("###.########")}}<#sep>,</#sep>
		  </#list>
        ];

        // Construct the polygon.
        var boundary = new google.maps.Polygon({
          paths: triangleCoords,
          strokeColor: '#FF0000',
          strokeOpacity: 0.8,
          strokeWeight: 2,
          fillColor: '#FF0000',
          fillOpacity: 0.35
        });
        boundary.setMap(map);
        
        initRoute();
        map.addListener('click', addLatLng);
        google.maps.event.addListener(boundary, 'click', addLatLng);
        
        footprint = new google.maps.Polyline({
          strokeColor: '#000000',
          strokeOpacity: 1.0,
          strokeWeight: 3
        });
        footprint.setMap(map);
      }

      // Handles click events on a map, and adds a new point to the Polyline.
      function addLatLng(event) {
        var path = poly.getPath();

        // Because path is an MVCArray, we can simply append a new coordinate
        // and it will automatically appear.
        path.push(event.latLng);
      }

      function initRoute() {
        poly = new google.maps.Polyline({
          strokeColor: '#00FF00',
          strokeOpacity: 1.0,
          strokeWeight: 3
        });
        poly.setMap(map);
      }
    </script>
    <script async defer
    src="https://maps.googleapis.com/maps/api/js?key=AIzaSyBTcLtx-BCBubx2EsVRnKotzxJsDUnvNDk&callback=initMap">
    </script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js">
    </script>