package com.liuyifei.hive.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import com.liuyifei.hive.common.Result;
import com.liuyifei.hive.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 使用 DataSource 操作 Hive
 */
@RestController
@RequestMapping("/hive")
public class HiveDataSourceController {

    private static final Logger logger = LoggerFactory.getLogger(HiveDataSourceController.class);

    @Autowired
    @Qualifier("hiveJdbcDataSource")
    org.apache.tomcat.jdbc.pool.DataSource jdbcDataSource;

    @Autowired
    @Qualifier("hiveDruidDataSource")
    DataSource druidDataSource;

    /**
     * 列举当前Hive库中的所有数据表
     */
    @RequestMapping("/table/list")
    public List<String> listAllTables() throws SQLException {
        List<String> list = new ArrayList<String>();
        // Statement statement = jdbcDataSource.getConnection().createStatement();
        Statement statement = druidDataSource.getConnection().createStatement();
        String sql = "show tables";
        logger.info("Running: " + sql);
        ResultSet res = statement.executeQuery(sql);
        while (res.next()) {
            list.add(res.getString(1));
        }
        return list;
    }

    /**
     * 查询Hive库中的某张数据表字段信息
     */
    @RequestMapping("/table/describe")
    public List<String> describeTable(String tableName) throws SQLException {
        List<String> list = new ArrayList<String>();
        // Statement statement = jdbcDataSource.getConnection().createStatement();
        Statement statement = druidDataSource.getConnection().createStatement();
        String sql = "describe " + tableName;
        logger.info("Running: " + sql);
        ResultSet res = statement.executeQuery(sql);
        while (res.next()) {
            list.add(res.getString(1));
        }
        return list;
    }

    /**
     * 查询订单整体的消费情况
     */
    @GetMapping("/getOverallConsumption")
    public Result<OverallConsumption> getOverallConsumption() throws SQLException {
        try {
            Statement statement = druidDataSource.getConnection().createStatement();
            String sql = "select  \n" +
                    "sum(purchase)  total_sales,\n" +
                    "count(distinct user_id) total_users,\n" +
                    "sum(purchase)/count(distinct user_id) avg_user_pay,\n" +
                    "sum(purchase)/count(purchase) avg_price\n" +
                    "from captain_data_shop_model";
            logger.info("Running: " + sql);
            ResultSet res = statement.executeQuery(sql);
            List<String> list = new ArrayList<>();
            int count = res.getMetaData().getColumnCount();
            while (res.next()) {
                for (int i = 1; i <= count; i++) {
                    list.add(res.getString(i));
                }
            }
            OverallConsumption overallConsumption = new OverallConsumption(list.get(0), list.get(1), list.get(2), list.get(3));
            logger.info("success");
            return Result.success(overallConsumption);
        } catch (Exception e) {
            logger.info("fail");
            return Result.fail(500, e.getMessage());
        }
    }

    /**
     * 查询用户性别比例
     */
    @GetMapping("/getUserGender")
    public Result<List<UserGender>> getUserGender() throws SQLException {
        try {
            Statement statement = druidDataSource.getConnection().createStatement();
            String sql = "select  \n" +
                    "Gender ,\n" +
                    "count(distinct User_ID)  User_num\n" +
                    "from captain_data_shop_model group by Gender\n";
            logger.info("Running: " + sql);
            ResultSet res = statement.executeQuery(sql);
            List<UserGender> userGenders = new ArrayList<>();
            while (res.next()) {
                UserGender userGender = new UserGender(res.getString("Gender"), res.getInt("User_num"));
                userGenders.add(userGender);
            }
            logger.info("success");
            return Result.success(userGenders);
        } catch (Exception e) {
            logger.info("fail");
            return Result.fail(500, e.getMessage());
        }
    }

    /**
     * 查询年龄比例
     */
    @GetMapping("/getUserAge")
    public Result<List<UserAge>> getUserAge() throws SQLException {
        try {
            Statement statement = druidDataSource.getConnection().createStatement();
            String sql = "select  \n" +
                    "Age ,\n" +
                    "count(distinct User_ID)  User_num\n" +
                    "from captain_data_shop_model group by Age\n";
            logger.info("Running: " + sql);
            ResultSet res = statement.executeQuery(sql);
            List<UserAge> userAges = new ArrayList<>();
            while (res.next()) {
                UserAge userAge = new UserAge(res.getString("Age"), res.getInt("User_num"));
                userAges.add(userAge);
            }
            logger.info("success");
            return Result.success(userAges);
        } catch (Exception e) {
            logger.info("fail");
            return Result.fail(500, e.getMessage());
        }
    }

    /**
     * 查询婚姻状况
     */
    @GetMapping("/getUserMarry")
    public Result<List<UserMarry>> getUserMarry() throws SQLException {
        try {
            Statement statement = druidDataSource.getConnection().createStatement();
            String sql = "select  \n" +
                    "Marital_Status ,\n" +
                    "count(distinct User_ID)  User_num\n" +
                    "from captain_data_shop_model group by Marital_Status\n";
            logger.info("Running: " + sql);
            ResultSet res = statement.executeQuery(sql);
            List<UserMarry> userMarrys = new ArrayList<>();
            while (res.next()) {
                UserMarry userMarry = new UserMarry(res.getInt("Marital_Status"), res.getInt("User_num"));
                userMarrys.add(userMarry);
            }
            logger.info("success");
            return Result.success(userMarrys);
        } catch (Exception e) {
            logger.info("fail");
            return Result.fail(500, e.getMessage());
        }
    }


    /**
     * 订单量Top
     */
    @GetMapping("/getTopOrder")
    public Result<List<OrderInfo>> getTopOrder() throws SQLException {
        try {
            Statement statement = druidDataSource.getConnection().createStatement();
            String sql = "with p as \n" +
                    "(\n" +
                    "SELECT \n" +
                    "Product_ID,\n" +
                    "count(Product_ID) sale_num,\n" +
                    "sum(Purchase) sum_pay\n" +
                    "FROM \n" +
                    "captain_data_shop_model\n" +
                    "GROUP BY\n" +
                    "Product_ID\n" +
                    ")\n" +
                    "\n" +
                    "SELECT * FROM \n" +
                    "(\n" +
                    "SELECT \n" +
                    "p.*,\n" +
                    "\n" +
                    "rank() over( order by sale_num desc) rank_sale_num\n" +
                    "FROM  \n" +
                    "p \n" +
                    ") t_1\n" +
                    "WHERE rank_sale_num<=10\n";
            logger.info("Running: " + sql);
            ResultSet res = statement.executeQuery(sql);
            List<OrderInfo> orderInfos = new ArrayList<>();
            while (res.next()) {
                OrderInfo orderInfo = new OrderInfo(res.getString("product_id"), res.getInt("sale_num"), res.getString("sum_pay"), res.getInt("rank_sale_num"));
                orderInfos.add(orderInfo);
            }
            logger.info("success");
            return Result.success(orderInfos);
        } catch (Exception e) {
            logger.info("fail");
            return Result.fail(500, e.getMessage());
        }
    }

    /**
     * 销售额Top
     */
    @GetMapping("/getTopPay")
    public Result<List<OrderInfo>> getTopPay() throws SQLException {
        try {
            Statement statement = druidDataSource.getConnection().createStatement();
            String sql = "with p as \n" +
                    "(\n" +
                    "SELECT \n" +
                    "Product_ID,\n" +
                    "count(Product_ID) sale_num,\n" +
                    "sum(Purchase) sum_pay\n" +
                    "FROM \n" +
                    "captain_data_shop_model\n" +
                    "GROUP BY\n" +
                    "Product_ID\n" +
                    ")\n" +
                    "\n" +
                    "SELECT * FROM \n" +
                    "(\n" +
                    "SELECT \n" +
                    "p.*,\n" +
                    "\n" +
                    "rank() over( order by sum_pay desc) rank_sale_num\n" +
                    "FROM  \n" +
                    "p \n" +
                    ") t_1\n" +
                    "WHERE rank_sale_num<=10\n";
            logger.info("Running: " + sql);
            ResultSet res = statement.executeQuery(sql);
            List<OrderInfo> orderInfos = new ArrayList<>();
            while (res.next()) {
                OrderInfo orderInfo = new OrderInfo(res.getString("product_id"), res.getInt("sale_num"), res.getString("sum_pay"), res.getInt("rank_sale_num"));
                orderInfos.add(orderInfo);
            }
            logger.info("success");
            return Result.success(orderInfos);
        } catch (Exception e) {
            logger.info("fail");
            return Result.fail(500, e.getMessage());
        }
    }

    /**
     * 组合查询
     */
    @GetMapping("/getCombination")
    public Result<List<OrderInfo>> getCombination(String age, String gender, String marry, String order) throws SQLException {
        try {
            ArrayList<String> conditions = new ArrayList<>();
            if (!Objects.equals(age, "")) {
                conditions.add("Age='" + age + "'");
            }
            if (!Objects.equals(gender, "")) {
                conditions.add("Gender='" + gender + "'");
            }
            if (!Objects.equals(marry, "")) {
                conditions.add("Marital_Status=" + marry + "");
            }

            boolean has_condition = false;
            String where_string = "";
            if (conditions.size() != 0) {
                has_condition = true;
                for (int index = 0; index < conditions.size(); index++) {
                    if (index==0){
                        where_string += "WHERE " + conditions.get(index) + " ";
                    }
                    else{
                        where_string += "AND " + conditions.get(index) + " ";
                    }
                }
            }


            Statement statement = druidDataSource.getConnection().createStatement();
            String sql = "with p as \n" +
                    "(\n" +
                    "SELECT \n" +
                    "Product_ID,\n" +
                    "count(Product_ID) sale_num,\n" +
                    "sum(Purchase) sum_pay\n" +
                    "FROM \n" +
                    "captain_data_shop_model\n" +
                    where_string + "\n" +
                    "GROUP BY\n" +
                    "Product_ID\n" +
                    ")\n" +
                    "\n" +
                    "SELECT * FROM \n" +
                    "(\n" +
                    "SELECT \n" +
                    "p.*,\n" +
                    "\n" +
                    "rank() over( order by "+ order +" desc) rank_sale_num\n" +
                    "FROM  \n" +
                    "p \n" +
                    ") t_1\n" +
                    "WHERE rank_sale_num<=10\n";
            logger.info("Running: " + sql);
            ResultSet res = statement.executeQuery(sql);
            List<OrderInfo> orderInfos = new ArrayList<>();
            while (res.next()) {
                OrderInfo orderInfo = new OrderInfo(res.getString("product_id"), res.getInt("sale_num"), res.getString("sum_pay"), res.getInt("rank_sale_num"));
                orderInfos.add(orderInfo);
            }
            logger.info("success");
            return Result.success(orderInfos);
        } catch (Exception e) {
            logger.info("fail");
            return Result.fail(500, e.getMessage());
        }
    }
}