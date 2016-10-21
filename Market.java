package exchangeStructures;

import orderSpecs.MarketId;
import orderSpecs.Side;
import orderTypes.SweepingOrder;

public class Market {

	private Exchange _exchange;
	private MarketId _marketId;
	private Book _bidBook;
	private Book _offerBook;
	
	public Market( Exchange exchange, MarketId marketId ) throws Exception {
		_exchange = exchange;
		_marketId = marketId;
		_bidBook = new Book( this, Side.BUY );
		_offerBook = new Book( this, Side.SELL );
		_bidBook.setOtherSide( _offerBook );
		_offerBook.setOtherSide( _bidBook );
	}
	
	public Exchange getExchange() { return _exchange; }
	public MarketId getMarketId() { return _marketId; }
	public Book getBidBook() { return _bidBook; }
	public Book getOfferBook() { return _offerBook; }
	
	public void sweep( SweepingOrder sweepingOrder ) throws Exception {
		if( sweepingOrder.getSide() == Side.BUY )
			_offerBook.sweep( sweepingOrder );
		else
			_bidBook.sweep( sweepingOrder );
	}
	
}
