package com.neueda.trade.server.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.neueda.trade.server.database.MarketDto;
import com.neueda.trade.server.database.StockDto;
import com.neueda.trade.server.model.Market;
import com.neueda.trade.server.model.Stock;
import com.neueda.trade.server.model.StockDao;
import com.neueda.trade.server.rules.Model;


@Component
public class MySqlStockDao implements StockDao {

   @Autowired
    private JdbcTemplate tpl;
    
    @Override
    public int rowCount() {
        return tpl.queryForObject("select count(*) from Stocks", Integer.class);
    }

    @Override
    public List<StockDto> findAll() {
        return this.tpl.query("select ticker, symbol, markets.ticker as \"market\", description from " +
                "Stocks inner join Markets on Stocks.market = Markets.ticker " +
                "order by ticker", new StockMapper());
    }

    @Override
    public Stock findById(String id) {
        List<StockDto> stocks = this.tpl.query(
                "select ticker, symbol, markets.ticker as \"market\", description from " +
                "Stocks inner join Markets on Stocks.market = Markets.ticker " +
                "where Markets.ticker = ?",
                new Object[]{id},
                new StockMapper()
        );
        return Model.validateFindUnique(stocks, id, "stock");
    }

    @Override
    public int place(Stock stock) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.tpl.update(
            new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                            connection.prepareStatement("insert into Stock (ticker, symbol, market, description) values (?, ?, ?, ?)");
                    ps.setString(1, stock.getTicker());
                    ps.setString(2, stock.getSymbol());
                    ps.setString(3, stock.getMarket().getTicker());
                    ps.setString(4, stock.getDescription());
                    return ps;
                }
            },
            keyHolder);
        return keyHolder.getKey().intValue();
    }

    private static final class StockMapper implements RowMapper<StockDto> {
        public StockDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            Market market = new MarketDto();
            market.setTicker(rs.getString("market"));
            StockDto stock = new StockDto();
            stock.setTicker(rs.getString("ticker"));
            stock.setSymbol(rs.getString("symbol"));
            stock.setMarket(market);
            stock.setDescription(rs.getString("description"));
            return stock;
        }
    }

}
