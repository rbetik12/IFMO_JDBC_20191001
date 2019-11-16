package com.efimchick.ifmo.web.jdbc.service;

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
import java.util.*;

public class ServiceFactory {

    private Map<BigInteger, Department> departments = getDepartments();
    private List<Employee> employees = getEmployees();
    private List<Employee> employeesWithManagerChain = getEmployeesWithManagerChain();

    private ResultSet getResultSet(String query) {
        try {
            Connection con = ConnectionSource.instance().createConnection();
            Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return statement.executeQuery(query);
        } catch (SQLException e) {
            return null;
        }
    }

    private List<Employee> getEmployees() {
        List<Employee> dbEmployees = new ArrayList<>();
        try {
            ResultSet rs = getResultSet("select * from employee");
            while (rs.next()) {
                dbEmployees.add(getEmployee(rs));
            }
        } catch (SQLException ignored) {
        }
        return dbEmployees;
    }

    private List<Employee> getEmployeesWithManagerChain() {
        List<Employee> dbEmployees = new ArrayList<>();
        ResultSet rs = getResultSet("select * from employee");
        try {
            while (rs.next()) {
                dbEmployees.add(getEmployeeWithManagerChain(rs));
            }
        } catch (SQLException ignored) {
        }
        return dbEmployees;
    }

    private Employee getEmployee(ResultSet resultSet) {
        try {
            Employee manager = null;
            if (resultSet.getString("manager") != null) {
                manager = getManager(resultSet, new BigInteger(resultSet.getString("manager")));
            }
            return mapEmployee(resultSet, manager);

        } catch (SQLException e) {
            return null;
        }
    }

    private Employee mapEmployee(ResultSet resultSet, Employee manager) throws SQLException {
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
                manager,
                resultSet.getString("department") == null ? null : departments.get(new BigInteger(resultSet.getString("department"))));
    }

    private Employee getManager(ResultSet resultSet, BigInteger managerID) {
        try {
            Employee manager = null;
            int rowPointer = resultSet.getRow();
            resultSet.beforeFirst();
            while (resultSet.next()) {
                if (new BigInteger(resultSet.getString("id")).equals(managerID)) {
                    manager = mapEmployee(resultSet, null);
                    break;
                }
            }
            resultSet.absolute(rowPointer);
            return manager;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<BigInteger, Department> getDepartments() {
        Map<BigInteger, Department> departmentsMap = new HashMap<>();
        try {
            ResultSet rs = getResultSet("select * from department");
            while (rs.next()) {
                departmentsMap.put(
                        new BigInteger(rs.getString("id")),
                        new Department(
                                new BigInteger(rs.getString("id")),
                                rs.getString("name"),
                                rs.getString("location")
                        )
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
        }
        return departmentsMap;
    }

    private Employee getEmployeeWithManagerChain(ResultSet resultSet) {
        try {
            Employee manager = null;
            if (resultSet.getString("manager") != null) {
                manager = getManagerWithChain(resultSet);
            }
            return mapEmployee(resultSet, manager);
        } catch (SQLException e) {
            return null;
        }
    }

    private Employee getManagerWithChain(ResultSet rs) {
        try {
            int currentRowID = rs.getRow();
            int managerID = rs.getInt("manager");
            if (managerID == 0) return null;
            Employee manager = null;
            rs.beforeFirst();
            while (rs.next()) {
                if (rs.getInt("id") == managerID) {
                    manager = getEmployeeWithManagerChain(rs);
                    break;
                }
            }
            rs.absolute(currentRowID);
            return manager;
        } catch (SQLException e) {
            return null;
        }
    }

    private List<Employee> getListPage(List<Employee> list, Paging paging) {
        //Copied that function from my group mates, because it fits perfectly here (Don't ban me plz)
        return list.subList(paging.itemPerPage * (paging.page - 1), Math.min(paging.itemPerPage * paging.page, list.size()));
    }

    public EmployeeService employeeService() {
        return new EmployeeService() {
            @Override
            public List<Employee> getAllSortByHireDate(Paging paging) {
                List<Employee> sortedEmployees = new ArrayList<>(employees);
                Collections.copy(sortedEmployees, employees);
                sortedEmployees.sort(Comparator.comparing(Employee::getHired));
                return getListPage(sortedEmployees, paging);
            }

            @Override
            public List<Employee> getAllSortByLastname(Paging paging) {
                List<Employee> sortedEmployees = new ArrayList<>(employees);
                sortedEmployees.sort(Comparator.comparing(o -> o.getFullName().getLastName()));
                return getListPage(sortedEmployees, paging);
            }

            @Override
            public List<Employee> getAllSortBySalary(Paging paging) {
                List<Employee> sortedEmployees = new ArrayList<>(employees);
                Collections.copy(sortedEmployees, employees);
                sortedEmployees.sort(Comparator.comparing(Employee::getSalary));
                return getListPage(sortedEmployees, paging);
            }

            @Override
            public List<Employee> getAllSortByDepartmentNameAndLastname(Paging paging) {
                List<Employee> sortedEmployees = new ArrayList<>(employees);
                Collections.copy(sortedEmployees, employees);
                sortedEmployees.sort(
                        (o1, o2) -> {
                            if (o1.getDepartment() != null && o2.getDepartment() != null) {
                                if (o1.getDepartment().equals(o2.getDepartment())) {
                                    return o1.getFullName().getLastName().compareTo(o2.getFullName().getLastName());
                                } else {
                                    return o1.getDepartment().getId().compareTo(o2.getDepartment().getId());
                                }
                            } else if (o1.getDepartment() == null) {
                                return -1;
                            } else if (o2.getDepartment() == null) {
                                return 1;
                            } else
                                return o1.getFullName().getLastName().compareTo(o2.getFullName().getLastName());
                        }
                );
                return getListPage(sortedEmployees, paging);
            }

            @Override
            public List<Employee> getByDepartmentSortByHireDate(Department department, Paging paging) {
                List<Employee> depEmployees = new ArrayList<>();
                for (Employee employee : employees) {
                    if (department.equals(employee.getDepartment()))
                        depEmployees.add(employee);
                }
                depEmployees.sort(Comparator.comparing(Employee::getHired));
                return getListPage(depEmployees, paging);
            }

            @Override
            public List<Employee> getByDepartmentSortBySalary(Department department, Paging paging) {
                List<Employee> depEmployees = new ArrayList<>();
                for (Employee employee : employees) {
                    if (department.equals(employee.getDepartment())) {
                        depEmployees.add(employee);
                    }
                }
                depEmployees.sort(Comparator.comparing(Employee::getSalary));
                return getListPage(depEmployees, paging);
            }

            @Override
            public List<Employee> getByDepartmentSortByLastname(Department department, Paging paging) {
                List<Employee> depEmployees = new ArrayList<>();
                for (Employee employee : employees) {
                    if (department.equals(employee.getDepartment()))
                        depEmployees.add(employee);
                }
                depEmployees.sort(Comparator.comparing(o -> o.getFullName().getLastName()));
                return getListPage(depEmployees, paging);
            }

            @Override
            public List<Employee> getByManagerSortByLastname(Employee manager, Paging paging) {
                List<Employee> managerEmployees = new ArrayList<>();
                for (Employee employee : employees) {
                    if (employee.getManager() != null && manager.getId().equals(employee.getManager().getId()))
                        managerEmployees.add(employee);
                }
                managerEmployees.sort(Comparator.comparing(o -> o.getFullName().getLastName()));
                return getListPage(managerEmployees, paging);
            }

            @Override
            public List<Employee> getByManagerSortByHireDate(Employee manager, Paging paging) {
                List<Employee> managerEmployees = new ArrayList<>();
                for (Employee employee : employees) {
//                    if (employee.getManager() != null)
//                        System.out.println("Employee's manager: " + employee.getManager());
//                    System.out.println("Manager: " + manager);
                    if (employee.getManager() != null && employee.getManager().getId().equals(manager.getId()))
//                        System.out.println("LMAOJNFV UEVUIFEKHJB");
                        managerEmployees.add(employee);

                }
                managerEmployees.sort(Comparator.comparing(Employee::getHired));
//                System.out.println(managerEmployees);
                return getListPage(managerEmployees, paging);
            }

            @Override
            public List<Employee> getByManagerSortBySalary(Employee manager, Paging paging) {
                List<Employee> managerEmployees = new ArrayList<>();
                for (Employee employee : employees) {
                    if (employee.getManager() != null && manager.getId().equals(employee.getManager().getId()))
                        managerEmployees.add(employee);
                }
                managerEmployees.sort(Comparator.comparing(Employee::getSalary));
                return getListPage(managerEmployees, paging);
            }

            @Override
            public Employee getWithDepartmentAndFullManagerChain(Employee employee) {
                for (Employee employee1 : employeesWithManagerChain) {
                    if (employee1.getId().equals(employee.getId()))
                        return employee1;
                }
                return null;
            }

            @Override
            public Employee getTopNthBySalaryByDepartment(int salaryRank, Department department) {
                List<Employee> depEmployees = new ArrayList<>();
                for (Employee employee : employees) {
                    if (department.equals(employee.getDepartment())) {
                        depEmployees.add(employee);
                    }
                }
                depEmployees.sort(
                        Comparator.comparing(Employee::getSalary)
                                .thenComparing((o1, o2) -> {
                                    if (o1.getId().compareTo(o2.getId()) > 0)
                                        return -1;
                                    else if (o1.getId().equals(o2.getId()))
                                        return 0;
                                    else
                                        return 1;
                                })
                );
                Collections.reverse(depEmployees);
                return depEmployees.get(salaryRank - 1);
            }
        };
    }
}
