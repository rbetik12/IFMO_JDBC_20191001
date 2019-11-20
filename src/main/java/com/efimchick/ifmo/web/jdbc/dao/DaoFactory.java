package com.efimchick.ifmo.web.jdbc.dao;

import com.efimchick.ifmo.web.jdbc.ConnectionSource;
import com.efimchick.ifmo.web.jdbc.domain.Department;
import com.efimchick.ifmo.web.jdbc.domain.Employee;
import com.efimchick.ifmo.web.jdbc.domain.FullName;
import com.efimchick.ifmo.web.jdbc.domain.Position;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DaoFactory {

    private ResultSet getResultSet(String query) {
        try {
            return ConnectionSource.instance().createConnection().createStatement().executeQuery(query);
        } catch (SQLException e) {
            return null;
        }
    }

    private List<Employee> getEmployeesList(ResultSet resultSet) {
        List<Employee> employees = new ArrayList<>();
        try {
            while (resultSet.next()) {
                employees.add(getEmployee(resultSet));
            }
        } catch (SQLException ignored) {
        }
        return employees;
    }

    public EmployeeDao employeeDAO() {

        return new EmployeeDao() {
            @Override
            public List<Employee> getByDepartment(Department department) {
                return getEmployeesList(getResultSet("select * from employee where department=" + department.getId()));
            }

            @Override
            public List<Employee> getByManager(Employee manager) {
                return getEmployeesList(getResultSet("select * from employee where manager=" + manager.getId()));
            }

            @Override
            public Optional<Employee> getById(BigInteger Id) {
                try {
                    ResultSet rs = getResultSet("select * from employee where id=" + Id.toString());
                    if (rs.next())
                        return Optional.of(getEmployee(rs));
                    else
                        return Optional.empty();
                } catch (SQLException e) {
                    return Optional.empty();
                }
            }

            @Override
            public List<Employee> getAll() {
                return getEmployeesList(getResultSet("select * from employee"));
            }

            @Override
            public Employee save(Employee employee) {
                try {
                    PreparedStatement preparedStatement = ConnectionSource.instance().createConnection().prepareStatement("insert into employee values (?,?,?,?,?,?,?,?,?)");
                    preparedStatement.setInt(1, employee.getId().intValue());
                    preparedStatement.setString(2, employee.getFullName().getFirstName());
                    preparedStatement.setString(3, employee.getFullName().getLastName());
                    preparedStatement.setString(4, employee.getFullName().getMiddleName());
                    preparedStatement.setString(5, employee.getPosition().toString());
                    preparedStatement.setInt(6, employee.getManagerId().intValue());
                    preparedStatement.setDate(7, Date.valueOf(employee.getHired()));
                    preparedStatement.setDouble(8, employee.getSalary().doubleValue());
                    preparedStatement.setInt(9, employee.getDepartmentId().intValue());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    return null;
                }
                return employee;
            }

            @Override
            public void delete(Employee employee) {
                try {
                    ConnectionSource.instance().createConnection().createStatement().execute("delete from employee where id=" + employee.getId().toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public DepartmentDao departmentDAO() {
        return new DepartmentDao() {
            @Override
            public Optional<Department> getById(BigInteger Id) {
                try {
                    ResultSet rs = getResultSet("select * from department where id=" + Id.toString());
                    if (rs.next())
                        return Optional.of(getDepartment(rs));
                    else
                        return Optional.empty();
                } catch (SQLException e) {
                    return Optional.empty();
                }
            }

            @Override
            public List<Department> getAll() {
                List<Department> deps = new ArrayList<>();
                try {
                    ResultSet rs = getResultSet("select * from department");
                    while (rs.next()) {
                        deps.add(getDepartment(rs));
                    }
                    return deps;
                } catch (SQLException e) {
                    return null;
                }
            }

            @Override
            public Department save(Department department) {
                try {
                    PreparedStatement preparedStatement;
                    if (getById(department.getId()).equals(Optional.empty())) {
                        preparedStatement = ConnectionSource.instance().createConnection().prepareStatement("INSERT INTO department VALUES (?,?,?)");
                        preparedStatement.setInt(1, department.getId().intValue());
                        preparedStatement.setString(2, department.getName());
                        preparedStatement.setString(3, department.getLocation());
                    } else {
                        preparedStatement = ConnectionSource.instance().createConnection().prepareStatement("UPDATE department SET NAME = ?, LOCATION = ? WHERE ID = ?");
                        preparedStatement.setString(1, department.getName());
                        preparedStatement.setString(2, department.getLocation());
                        preparedStatement.setInt(3, department.getId().intValue());
                    }
                    preparedStatement.executeUpdate();
                    return department;
                } catch (SQLException e) {
                    return null;
                }
            }

            @Override
            public void delete(Department department) {
                try {
                    ConnectionSource.instance().createConnection().createStatement().executeUpdate("delete from department where id=" + department.getId().toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        };
    }

    private Employee getEmployee(ResultSet resultSet) {
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
                    new BigDecimal(resultSet.getString("salary")),
                    resultSet.getString("manager") == null ? BigInteger.ZERO : new BigInteger(resultSet.getString("manager")),
                    resultSet.getString("department") == null ? BigInteger.ZERO : new BigInteger(resultSet.getString("department"))
            );
        } catch (SQLException e) {
            return null;
        }
    }

    private Department getDepartment(ResultSet resultSet) {
        try {
            return new Department(
                    new BigInteger(resultSet.getString("id")),
                    resultSet.getString("name"),
                    resultSet.getString("location")
            );
        } catch (SQLException e) {
            return null;
        }
    }
}
