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
import com.neueda.trade.server.model.Market;
import com.neueda.trade.server.model.MarketDao;
import com.neueda.trade.server.rules.Model;


@Component
public class MySqlMarketDao implements MarketDao {

    @Autowired
    private JdbcTemplate tpl;

    @Override
    public int rowCount() {
        return tpl.queryForObject("select count(*) from Markets", Integer.class);
    }

    @Override
    public List<MarketDto> findAll() {
        return this.tpl.query("select ticker, description from Markets order by ticker", new MarketMapper());
    }


    @Override
    public Market findById(String id) {
        List<MarketDto> markets = this.tpl.query(
                "select ticker, description from Markets where ticker = ?",
                new Object[]{id},
                new MarketMapper()
        );
        return Model.validateFindUnique(markets, id, "market");
    }

    @Override
    public int place(Market market) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.tpl.update(
            new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                            connection.prepareStatement("insert into Stock (ticker, description) values (?, ?)");
                    ps.setString(1, market.getTicker());
                    ps.setString(2, market.getDescription());
                    return ps;
                }
            },
            keyHolder);
        return keyHolder.getKey().intValue();
    }

    private static final class MarketMapper implements RowMapper<MarketDto> {
        public MarketDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            MarketDto market = new MarketDto();
            market.setTicker(rs.getString("ticker"));
            market.setDescription(rs.getString("description"));
            return market;
        }
    }

}

