package springboot.billgates.domain.billing.batch.sql;

public class BillingSqls {
    public static final String SELECT_JOINED_DATA = """
      SELECT
         m.member_id,
         m.email,
         m.phone_number,
         m.use_dnd,
         m.dnd_start_time,
         m.dnd_end_time,
         i.category,
         i.name AS item_name,
         u.amount
      FROM USAGE_HISTORY u
      JOIN MEMBER m ON m.member_id = u.member_id
      JOIN ITEM i ON i.item_id = u.item_id
      WHERE u.usage_date >= ?
      AND u.usage_date < ?
      ORDER BY u.member_id;
    """;

    public static final String SELECT_MEMBER_POLICIES = """
        SELECT
            dp.policy_id,
            dp.name,
            dp.discount_type,
            dp.discount_value,
            dp.target_category
        FROM member_discount_policy mdp
        JOIN discount_policy dp ON dp.policy_id = mdp.policy_id
        WHERE mdp.member_id = ?
    """;

    public static final String INSERT_BILLING = """
        INSERT INTO BILLING (
            billing_id,
            member_id,
            billing_month,
            original_amount,
            total_discount_amount,
            total_amount,
            created_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;
    public static final String INSERT_BILLING_ITEM = """
        INSERT INTO BILLING_ITEM (
            billing_id,
            category,
            item_name,
            amount
        )
        VALUES (?, ?, ?, ?)
     """;

    public static final String INSERT_MESSAGE = """
        INSERT INTO MESSAGE (
            message_id, 
            member_id, 
            billing_id, 
            channel, 
            status, 
            reserved_at, 
            created_at, 
            template_code,
            title,
            content,
            email,
            phone_number
        ) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    public static final String INSERT_BILLING_DISCOUNT = """
        INSERT INTO billing_discount (
            discount_id, 
            billing_id, 
            policy_id, 
            discount_name, 
            discount_amount, 
            target_category
        )
        VALUES (?, ?, ?, ?, ?, ?)
    """;
}
