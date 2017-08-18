package com.neueda.trade.server.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.neueda.trade.server.TradeException;
import com.neueda.trade.server.database.StockDto;
import com.neueda.trade.server.database.TradeDto;
import com.neueda.trade.server.model.BuySell;
import com.neueda.trade.server.model.Stock;
import com.neueda.trade.server.model.Trade;
import com.neueda.trade.server.model.TradeDao;
import com.neueda.trade.server.model.TradeState;
import com.neueda.trade.server.rules.Model;

/**
 * MySQL DAO for to access the Trade table.
 *
 * @author Neueda
 *
 */
@Component
public class MySqlTradeDao implements TradeDao {

    @Autowired
    private JdbcTemplate tpl;

    @Override
    public int rowCount() {
        return tpl.queryForObject("select count(*) from Trades", Integer.class);
    }

    @Override
    public List<Trade> findAll() {
        return this.tpl.query("select id, transid, stock, ptime, price, volume, buysell, state, stime from Trades order by transid", new TradeMapper());
    }

    @Override
    public Trade findById(int id) {
        List<Trade> trades = this.tpl.query(
                "select id, transid, stock, ptime, price, volume, buysell, state, stime from Trades where id = ?",
                new Object[]{id},
                new TradeMapper()
        );
        return Model.validateFindUnique(trades, id, "trade id");
    }

    @Override
    public int place(Trade trade) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.tpl.update(
            new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                            connection.prepareStatement("insert into Trades (transid, stock, ptime, price, volume, buysell, state, stime) values (?, ?, ?, ?, ?, ?, ?, ?)",
                            		                    Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, trade.getTransid());
                    ps.setString(2, trade.getStock().getTicker());
                    ps.setTimestamp(3, new Timestamp(trade.getPtime().getTime()));
                    ps.setDouble(4, trade.getPrice());
                    ps.setInt(5, trade.getVolume());
                    ps.setString(6, Model.fromJson(Model.toJson(trade.getBuysell()), String.class));
                    ps.setString(7, Model.fromJson(Model.toJson(trade.getState()), String.class));
                    ps.setTimestamp(8, new Timestamp(trade.getStime().getTime()));
                    return ps;
                }
            },
            keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public Trade updateState(Trade trade) {
        int count = this.tpl.update(
                "update Trades set state=?, stime=? where id=?",
                Model.fromJson(Model.toJson(trade.getState()), String.class),
                new Timestamp(trade.getStime().getTime()),
                trade.getId());
        if (count != 1) {
            throw new TradeException("Update failed: trade %d not found", trade.getId());
        }
    	return trade;
    }

    @Override
	public int clear() {
        return this.tpl.update("delete from Trades");
	}

	private static final class TradeMapper implements RowMapper<Trade> {
        public Trade mapRow(ResultSet rs, int rowNum) throws SQLException {
            Stock stock = new StockDto();
            stock.setTicker(rs.getString("stock"));
            Trade trade = new TradeDto();
            trade.setId(rs.getInt("id"));
            trade.setTransid(rs.getString("transid"));
			trade.setStock(stock);
			trade.setPtime(rs.getTimestamp("ptime"));
			trade.setPrice(rs.getDouble("price"));
			trade.setVolume(rs.getInt("volume"));
			trade.setBuysell(Model.fromJson(Model.toJson(rs.getString("buysell")), BuySell.class));
			trade.setState(Model.fromJson(Model.toJson(rs.getString("state")), TradeState.class));
			trade.setStime(rs.getTimestamp("stime"));
			return trade;
        }
    }


    // TODO: Extension solutions start here

	@Override
	public Trade findByTransid(String transid) {
        List<Trade> trades = this.tpl.query(
                "select id, transid, stock, ptime, price, volume, buysell, state, stime from Trades where transid = ?",
                new Object[]{transid},
                new TradeMapper()
        );
        return Model.validateFindUnique(trades, transid, "trade transaction id");
	}

    @Override
    public int modify(Trade trade) {
        int count = this.tpl.update(
                "update Trades set price=?, volume=?, state=?, stime=? where id=?",
                trade.getPrice(),
                trade.getVolume(),
                Model.fromJson(Model.toJson(trade.getState()), String.class),
                new Timestamp(trade.getStime().getTime()),
                trade.getId());
        if (count != 1) {
            throw new TradeException("Update failed: trade %d not found", trade.getId());
        }
    	return trade.getId();
    }

}
