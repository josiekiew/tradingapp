var logging = true;

function LOG(obj) {
    if (!logging) return;
    if (obj instanceof Object)
        console.log(JSON.stringify(obj));
    else
        console.log(obj);
//    console.log(JSON.stringify(new Error().stack));
}

var tradeApp = angular.module('tradeApp', ['ngMessages']);

tradeApp.controller('layoutCtrl', ['$scope', function ($scope) {
   
	$scope.setMessage = function(message) {
		$scope.message = message;
	};

	$scope.setPanel = function(panel) {
		$scope.showPanel = panel;
		$scope.setMessage(null);
	};

	$scope.clearMessages = function() {
		  $scope.message = null;
		  $scope.showError = false;
          $scope.$error = {};
    };
   
    $scope.setError = function (msg) {
       if (msg instanceof Object) {
    	   // application or REST error
           if ('error' in msg) {
        	   if (msg.error instanceof Object && 
        			   ('path' in msg.error || 'error' in msg.error)) {
        		   // dig message out of Spring REST error
        		   if ('path' in msg.error) {
        			   msg = msg.error.path + ": " + msg.error.error;
        		   }
        		   else {
        			   msg = msg.error.error;
        		   }
        	   }
        	   else {
                   msg = msg.error.message;
        	   }
           }
           else if ('fatal' in msg) {
               msg = "Load Data Error: "+msg.fatal.message;
           }
           else {
               msg = "Invalid response: "+JSON.stringify(msg);
           }
       }
       $scope.message = null;
       $scope.$error = { "failed": msg };
       $scope.showError = true;
    };
   
    $scope.httpError = function(response) {
    	LOG(response);
	   if ('data' in response && response.data !== null) {
		   $scope.setError(response.data);
	   }
	   else if (response.status === 0) {
	       $scope.setError("Connection refused: "+response.config.url);		   
	   }
	   else {
	       $scope.setError("Load Data Error: "+response.status+" - "+response.statusText);		   
	   }
    };
   
    $scope.clearMessages();
    $scope.setPanel('none');
}]);

tradeApp.controller('tradeListCtrl', ['$scope', '$http', function ($scope, $http) {
  	
    $scope.doList = function() {
	  $scope.clearMessages();
	  $scope.setMessage("loading trade list...");
      $http.get('trades/list').then(
         function(response) {
            LOG(response);
            var data = response.data;
            $scope.setMessage(null);
            if (data instanceof Array) {
                $scope.trades = data;
                $scope.title = "Trade List (total: "+$scope.trades.length+")";
                $scope.setPanel('list');
            }
            else {
                $scope.setError(data);
            }
         },
         $scope.httpError
      );
  };

  $scope.doClear = function() {
	  if (!confirm("Are you sure you want do an EOD reconsilliation\n which will clear all trades from the System?")) {
		  return;
	  }
	  $scope.clearMessages();
	  $scope.setMessage("EOD reconciliation...");
      $http.get('trades/reconcile').then(
         function(response) {
            LOG(response);
            var data = response.data;
            if (data instanceof Object && "count" in data) {
                $scope.eodCount = data.count;
                $scope.title = "EOD reconciliation";
                $scope.setPanel('clear');
            }
            else {
                $scope.setError(data);
            }
         },
         $scope.httpError
     );
  };

  $scope.doPlace = function() {
	  $scope.clearMessages();
	  $scope.title = "Place Trade";
      $scope.setPanel('edit');
      $scope.updateTrade = false;
      $scope.fields = {'id': 0, 'transid': null, 'stock': {ticker: null},  'ptime': null, 
	           'price': null, 'volume': null, 'buysell': null, 'state': null, 'stime': null };
  };

  $scope.updateTrade = false;

  $scope.doSubmit = function(update) {
	  $scope.clearMessages();
	  var data = $scope.fields;
      data.ptime = data.date;
      data.ptime.setHours(data.time.getHours(),data.time.getMinutes());
      LOG(data);
      $scope.setMessage("saving data for "+data.transid);
      var url = update ? "trades/modify/"+data.transid : "trades/place" ;
      $http.post(url, data).then(
            function(response) {
               var data = response.data;
               LOG(response);
               $scope.setMessage(null);
               $scope.setPanel('none');
               $scope.title = "";
               if (data instanceof Object && 'id' in data) {
                   $scope.doList();
               }
               else {
                   $scope.setError(data);
               }
            },
            $scope.httpError
      );
    }

    $scope.doRestSubmit = function(update) {
    	$scope.clearMessages();
    	$scope.setMessage("saving data for "+$scope.fields.transid);
        data.ptime = data.date;
        data.ptime.setHours(data.time.getHours(),data.time.getMinutes());
        $http({
              method: 'POST',
              url:    update ? "trades/modify/"+data.transid : "trades/place",
              data:   $scope.fields,
              headers: {
                  "Content-Type": "application/json",
                  "Accept": "application/json"
              }
          }).then(
              function(response) {
                 var data = response.data;
                 LOG(response);
                 $scope.setPanel('none');
                 $scope.title = "";
                 if (data instanceof Object && 'id' in data) {
                     $scope.doList();
                 }
                 else {
                     $scope.setError(data);
                 }
              },
              $scope.httpError
        );
    }


    $scope.doEdit = function(trade) {
    	$scope.clearMessages();
    	$scope.setMessage("loading data for "+trade.transid);
        $http.get('trades/find/'+trade.id).then(
           function(response) {
              var data = response.data;
              if (data instanceof Object && 'id' in data) {
            	  data.date = new Date(data.ptime);
                  data.time = new Date(data.date);
                  $scope.fields = data;
                  $scope.title = "Edit TransID: "+data.transid;
                  $scope.setPanel('edit');
                  $scope.updateTrade = true;
              }
              else {
                  $scope.setError(data);
              }
           },
           $scope.httpError
        );
   };

   $scope.doCancel = function(trade) {
	   $scope.clearMessages();
	   $scope.setMessage("cancelling entry for "+trade.transid);
       $http.get('trades/cancel/'+trade.transid).then(
           function(response) {
              var data = response.data;
              if (data instanceof Object && 'id' in data) {
                  $scope.doList();
              }
              else {
                  $scope.setError(data);
              }
           },
           $scope.httpError
       );
   };

   $scope.doState = function(transid, state) {
	   $scope.clearMessages();
	   $scope.setMessage("State change for "+transid+" to "+state);
	   switch(state) {
	    case "A": action = 'accept'; break;
	    case "D": action = 'deny'; break;
	    case "E": action = 'execute'; break;
	    case "R": action = 'reject'; break;
	    case "S": action = 'settle'; break;
	    default:
	    	alert("State not yet supported: "+state);
	    	$scope.setError({"error":{message:"State not yet supported: "+state}})
	    	return;
	   }
       $http.get('trades/'+action+'/'+transid).then(
           function(response) {
              var data = response.data;
              if (data instanceof Object && 'id' in data) {
                  $scope.doList();
              }
              else {
                  $scope.setError(data);
              }
           },
           $scope.httpError
       );
   };
   $scope.NOW = Date.now();
   $scope.clearMessages();
   $scope.trades = [];
}]);

tradeApp.controller('paginationCtrl', ['$scope', function ($scope) {
  $scope.itemsPerPage = 5;
  $scope.currentPage = 0;
  $scope.pageCount = 0;

  $scope.range = function(items) {
    var list = [];
    var page = 0;
    for (var i=0; i<items.length; i+=$scope.itemsPerPage){
        list.push(page++);
    }
    $scope.pageCount = (items.length/$scope.itemsPerPage)-1;
    return list;
  };

  $scope.prevPage = function() {
    if ($scope.currentPage > 0) {
      $scope.currentPage--;
    }
  };

  $scope.prevPageDisabled = function() {
    return $scope.currentPage === 0 ? "disabled" : "";
  };

  $scope.nextPage = function() {
    if ($scope.currentPage < $scope.pageCount) {
      $scope.currentPage++;
    }
  };

  $scope.nextPageDisabled = function() {
    return $scope.currentPage === $scope.pageCount ? "disabled" : "";
  };

  $scope.setPage = function(n) {
    $scope.currentPage = n;
  };

}]);