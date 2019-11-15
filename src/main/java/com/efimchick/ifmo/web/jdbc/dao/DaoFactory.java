package com.efimchick.ifmo.web.jdbc.dao;

import com.efimchick.ifmo.web.jdbc.ConnectionSource;
import com.efimchick.ifmo.web.jdbc.domain.Department;
import com.efimchick.ifmo.web.jdbc.domain.Employee;
import com.efimchick.ifmo.web.jdbc.domain.FullName;
import com.efimchick.ifmo.web.jdbc.domain.Position;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DaoFactory {

    private List<Employee> employees = getEmployeesFromDB();
    private List<Department> departments = getDeparmentsFromDB();

    private List<Department> getDeparmentsFromDB() {
        List<Department> departments = new ArrayList<>();
        try {
            Connection con = ConnectionSource.instance().createConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery("select * from department");
            while (rs.next()) {
                departments.add(getDepartment(rs));
            }
        } catch (SQLException ignored) {
        }
        return departments;
    }

    private List<Employee> getEmployeesFromDB() {
        List<Employee> employees = new ArrayList<>();
        try {
            Connection con = ConnectionSource.instance().createConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery("select * from employee");
            while (rs.next()) {
                employees.add(getEmployee(rs));
            }
        } catch (SQLException ignored) {
        }
        return employees;
    }

    public EmployeeDao employeeDAO() {

        return new EmployeeDao() {
            @Override
            public List<Employee> getByDepartment(Department department) {
                List<Employee> depEmployees = new ArrayList<>();
                for (Employee employee : employees) {
                    if (employee.getDepartmentId().equals(department.getId())) depEmployees.add(employee);
                }
                return depEmployees;
            }

            @Override
            public List<Employee> getByManager(Employee manager) {
                List<Employee> managerEmployees = new ArrayList<>();
                for (Employee employee : employees) {
                    if (employee.getManagerId().equals(manager.getId())) managerEmployees.add(employee);
                }
                return managerEmployees;
            }

            @Override
            public Optional<Employee> getById(BigInteger Id) {
                for (Employee employee : employees) {
                    if (employee.getId().equals(Id)) return Optional.of(employee);
                }
                return Optional.empty();
            }

            @Override
            public List<Employee> getAll() {
                return employees;
            }

            @Override
            public Employee save(Employee employee) {
                employees.add(employee);
                return employee;
            }

            @Override
            public void delete(Employee employee) {
                employees.remove(employee);
            }
        };
    }

    public DepartmentDao departmentDAO() {
        return new DepartmentDao() {
            @Override
            public Optional<Department> getById(BigInteger Id) {
                for (Department dep : departments) {
                    if (dep.getId().equals(Id)) return Optional.of(dep);
                }
                return Optional.empty();
            }

            @Override
            public List<Department> getAll() {
                return departments;
            }

            @Override
            public Department save(Department department) {
                for (Department dep: departments) {
                    if (dep.getId().equals(department.getId())) {
                        departments.remove(dep);
                        break;
                    }
                }
                departments.add(department);
                return department;
            }

            @Override
            public void delete(Department department) {
                departments.remove(department);
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
