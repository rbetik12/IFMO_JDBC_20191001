package com.efimchick.ifmo.web.jdbc;

/**
 * Implement sql queries like described
 */
public class SqlQueries {
    //Select all employees sorted by last name in ascending order
    //language=HSQLDB
    String select01 = "select * from employee order by lastname asc";

    //Select employees having no more than 5 characters in last name sorted by last name in ascending order
    //language=HSQLDB
    String select02 = "select * from employee where length(lastname) <= 5 order by lastname asc";

    //Select employees having salary no less than 2000 and no more than 3000
    //language=HSQLDB
    String select03 = "select * from employee where salary >= 2000 and salary <= 3000";

    //Select employees having salary no more than 2000 or no less than 3000
    //language=HSQLDB
    String select04 = "select * from employee where salary <= 2000 or salary >= 3000";

    //Select employees assigned to a department and corresponding department name
    //language=HSQLDB
    String select05 = "select * from employee, department where employee.department is not null and employee.department = department.id";

    //Select all employees and corresponding department name if there is one.
    //Name column containing name of the department "depname".
    //language=HSQLDB
    String select06 = "select employee.id, employee.firstname, employee.department, employee.lastname, employee.salary, employee.middlename, employee.position, employee.manager, employee.manager, employee.hiredate,department.id, department.name as \"depname\" from employee full join department on employee.department = department.id where employee.middlename is not null";

    //Select total salary pf all employees. Name it "total".
    //language=HSQLDB
    String select07 = "select sum(salary) as \"total\" from employee";

    //Select all departments and amount of employees assigned per department
    //Name column containing name of the department "depname".
    //Name column containing employee amount "staff_size".
    //language=HSQLDB
    String select08 = "select department.id, department.name as \"depname\", department.location, count(employee.department) as \"staff_size\" from department inner join employee on department.id = employee.department group by department.id";

    //Select all departments and values of total and average salary per department
    //Name column containing name of the department "depname".
    //language=HSQLDB
    String select09 = "select department.id, department.name as \"depname\", sum(employee.salary) as \"total\", avg(employee.salary) as \"average\" from department inner join employee on department.id = employee.department group by department.id";

    //Select all employees and their managers if there is one.
    //Name column containing employee lastname "employee".
    //Name column containing manager lastname "manager".
    //language=HSQLDB
    String select10 = "select e.lastname as \"employee\", e_m.lastname as \"manager\" from employee e left join employee e_m on e.manager = e_m.id";


}
