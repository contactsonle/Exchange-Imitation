package exchangeStructures;

import java.util.HashMap;

import messages.Cancel;
import messages.CancelRejected;
import messages.Cancelled;
import messages.RestingOrderConfirmation;
import orderSpecs.ClientOrderId;
import orderSpecs.MarketId;
import orderTypes.RestingOrder;
import orderTypes.SweepingOrder;

/**
 * Implements an exchange with markets that each contain an
 * offer book and a bid book. Communicates with clients
 * using its Comms link. (We fake that part. There are no
 * clients, so all of the messages sent by the exchange to
 * clients are just saved in lists inside the Comms link
 * and can be retrieved for unit testing purposes.)
 *
 */
public class Exchange {

 /** The comms link that will simulate communications with clients */
 private Comms _comms;
 
 /** A data structure that contains alll resting orders indexed
  * by their clientOrderId
  */
 private HashMap<ClientOrderId,RestingOrder> _restingOrders;
 
//A data structure that contains the market ID and Market
 private HashMap<MarketId,Market> _markets;
 
 /**
  * Instantiate a new exchange, create a comms link and data
  * for market and resting orders
  */
 public Exchange() {
  
  // Instantiate a new comms link for simulating
  // communication with clients
  _comms = new Comms();
  
  //Instantiate new restingOrders lists and markets list
  _restingOrders = new HashMap<ClientOrderId,RestingOrder>();
  _markets = new HashMap<MarketId,Market>();

  //_comms.cancelled( new Cancelled( cancelMsg.getClientId(), cancelMsg.getClientOrderId() ) );
  }
 
 
 /**
  * @return Return the communications link that is used to
  *         simulate communications with clients
  */
 public Comms getComms() {   return _comms; }

 /** Remove resting order from global list of all resting orders
  * 
  * @param restingOrder Resting order to remove from list
  */
 public void unregisterOrder(RestingOrder restingOrder) {
  // Remove unregisterOrder
   _restingOrders.remove(restingOrder.getClientOrderId());
 }

 /** 
  * @return Map of all markets indexed by their market ids
  */
 public HashMap<MarketId,Market> getMarkets() { return _markets; }
 
 //getMarket method to get the market with input marketID
 public Market getMarket(MarketId mid) {
   return _markets.get(mid);
 }

 //Put new market
 public void addMarket(Market m) {
   _markets.put(m.getMarketId(), m);
 }
 
 //Sweep method for Exchange class
 public void sweep(SweepingOrder sweepingOrder) throws Exception {
   for (MarketId mid: _markets.keySet()) {
     _markets.get(mid).sweep(sweepingOrder);
   }
 }
 
 public void cancel(Cancel cancelMsg) throws Exception {
   //Cancel if client wants to and it's possible
   if (_restingOrders.keySet().contains(cancelMsg.getClientOrderId())) {
     _comms.cancelled( new Cancelled( cancelMsg.getClientId(), cancelMsg.getClientOrderId() ) );
     _restingOrders.get(cancelMsg.getClientOrderId()).cancel();
     _restingOrders.remove(cancelMsg.getClientOrderId());
   //Can't cancel because already made  
   } else {
     _comms.sendCancelRejected( new CancelRejected( cancelMsg.getClientId(), cancelMsg.getClientOrderId() ) );
   }
 }
 
 //Record Resting order and report
 public void registerRestingOrder(RestingOrder ro) {
   _restingOrders.put(ro.getClientOrderId(),ro);
   _comms.sendRestingOrderConfirmation(new RestingOrderConfirmation(ro));
 }
 
 /**
  * @return Global map of resting orders indexed by their client
  *         order ids
  */
 public HashMap<ClientOrderId,RestingOrder> getRestingOrdersMap() { return _restingOrders; }

}
