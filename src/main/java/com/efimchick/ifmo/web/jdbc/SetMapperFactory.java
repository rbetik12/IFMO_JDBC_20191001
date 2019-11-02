package com.efimchick.ifmo.web.jdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.efimchick.ifmo.web.jdbc.domain.Employee;
import com.efimchick.ifmo.web.jdbc.domain.FullName;
import com.efimchick.ifmo.web.jdbc.domain.Position;

public class SetMapperFactory {

    public SetMapper<Set<Employee>> employeesSetMapper() {
        return new SetMapper<Set<Employee>>() {
            @Override
            public Set<Employee> mapSet(ResultSet resultSet) {
                Set<Employee> employees = new HashSet<>();
                try {
                    while (resultSet.next()) {
                        employees.add(getEmployee(resultSet));
                    }
                } catch (SQLException ignored) {

                }
                return employees;
            }
        };
    }

    private Employee getEmployee(ResultSet rs) {
        try {
            return new Employee(
                    new BigInteger(rs.getString("id")),
                    new FullName(
                            rs.getString("firstname"),
                            rs.getString("lastname"),
                            rs.getString("middlename")
                    ),
                    Position.valueOf(rs.getString("position")),
                    LocalDate.parse(rs.getString("hiredate")),
                    new BigDecimal(rs.getString("salary")),
                    getManager(rs)
            );
        } catch (SQLException e) {
            return null;
        }
    }

    private Employee getManager(ResultSet rs) throws SQLException {
        int currentRowID = rs.getRow();
        int managerID = rs.getInt("manager");
        if (managerID == 0) return null;
        Employee manager = null;
        rs.beforeFirst();
        while (rs.next()) {
            if (rs.getInt("id") == managerID) {
                manager = getEmployee(rs);
                break;
            }
        }
        rs.absolute(currentRowID);
        return manager;
    }
}
