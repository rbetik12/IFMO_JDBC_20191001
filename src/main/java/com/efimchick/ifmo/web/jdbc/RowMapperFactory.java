package com.efimchick.ifmo.web.jdbc;

import com.efimchick.ifmo.web.jdbc.domain.Employee;
import com.efimchick.ifmo.web.jdbc.domain.FullName;
import com.efimchick.ifmo.web.jdbc.domain.Position;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class RowMapperFactory {

    public RowMapper<Employee> employeeRowMapper() {
        return new RowMapper<Employee>() {
            @Override
            public Employee mapRow(ResultSet resultSet){
                try {
                    return new Employee(
                            new BigInteger(resultSet.getString("id")),
                            new FullName(
                                    resultSet.getString("firstname"),
                                    resultSet.getString("lastname"),
                                    resultSet.getString("middlename")
                            ),
                            Position.valueOf(resultSet.getString("position")),
                            LocalDate.parse(resultSet.getString("hiredate")),
                            new BigDecimal(resultSet.getString("salary"))
                    );
                } catch (SQLException e) {
                    return null;
                }
            }
        };
    }
}

