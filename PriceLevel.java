package exchangeStructures;

import java.util.Iterator;
import java.util.LinkedList;

import fills.Fill;
import orderSpecs.Price;
import orderSpecs.Quantity;
import orderTypes.RestingOrder;
import orderTypes.SweepingOrder;

/** This class represents one price level in either the bid
/*  or offer book of one market. It allows a sweeping order
/*  to sweep the resting order in its list of resting orders,
/*  matching until the sweeping order is either filled or
/*  the list of orders at this price level is exhausted.
*/
public class PriceLevel {

	private Price _price;
	private LinkedList<RestingOrder> _orders;

	/** Create a new price level and save the price.
	/* Instantiate a list for holding orders.
	*/	
	public PriceLevel( Price price ) {
		_price = new Price( price );
		_orders = new LinkedList<RestingOrder>();
	}

	/** Return the price of this price level */	
	public final Price getPrice() { return _price; }
	
	/** This is where the sweeping order sweeps the price level
	/*  looking for resting orders to match with
	*/
	public boolean sweep( Market market, SweepingOrder sweepingOrder ) throws Exception {
		Iterator<RestingOrder> restingOrdersIterator = _orders.iterator();
		while( restingOrdersIterator.hasNext() ) {
			RestingOrder restingOrder = restingOrdersIterator.next();
			if( restingOrder.isFilled() ) {
				// Its quantity is zero
				// Don't match with it and remove it from the book
				restingOrdersIterator.remove();
			} else {
				// This resting order has some quantity that can be
				// matched with.
				long sweepingQty = sweepingOrder.getQuantity().getValue();
				long restingQty = restingOrder.getQuantity().getValue();
				long matchQty = Math.min( sweepingQty, restingQty );
				// Create fills
				// Send counter party fill
				market.getExchange().getComms().sendFill( 
					new Fill( 
						restingOrder.getClientId(), // Counterparty id
						sweepingOrder.getClientId(), // Client id
						restingOrder.getClientOrderId(), // Counterparty order id
						new Quantity( matchQty ) 
					) 
				);
				// Send client fill
				market.getExchange().getComms().sendFill(
					new Fill( 
						sweepingOrder.getClientId(), // Client id
						restingOrder.getClientId(), // Counterparty id
						sweepingOrder.getClientOrderId(), // Client order id
						new Quantity( matchQty ) 
					) 
				);
				// Reduce the sweeping order by the matched quantity
				sweepingOrder.reduceQtyBy( new Quantity( matchQty ) );
				// Reduce the resting order by the matched quantity
				restingOrder.reduceQtyBy( new Quantity( matchQty ) );
				// Did we completely fill the resting order?
				if( restingOrder.isFilled() ) {
					// Yes, we completely filled the resting order, so
					// tell the exchange it should no longer see this 
					// resting order and remove it from the book
					market.getExchange().unregisterOrder( restingOrder );
					restingOrdersIterator.remove();
					// Did we completely fill the sweeping order?
					if( sweepingOrder.isFilled() )
						// Yes, so stop sweeping and return true 
						// if this price level should be removed
						// and false otherwise
						return !restingOrdersIterator.hasNext();
				} else {
					// No, there is some quantity left in the resting
					// order. If the full amount of the resting order 
					// was not matched, it means the full amount of the 
					// sweeping order was matched, so exit this method 
					// and tell the calling method that this price level 
					// should not be removed.
					return false;
				}
			}
		}
		// If we're here, we've iterated through all of the
		// resting orders at this price level, presumably 
		// because our sweeping order matched with and 
		// completely filled all of them. Signal that this
		// price level should be removed.
		return true; 
	}
	
	/**
	 * Add a resting order to this price level's time queue
	 * @param restingOrder
	 */
	public void makeRestingOrder( RestingOrder restingOrder ) {
		_orders.addLast( restingOrder );
	}

	/** Return list of resting orders at this price level */	
	public LinkedList<RestingOrder> getOrders() { return _orders; }
	
}
