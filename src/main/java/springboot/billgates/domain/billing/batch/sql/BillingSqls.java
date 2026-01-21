package springboot.billgates.domain.billing.batch.sql;

public class BillingSqls {
    public static final String SELECT_MEMBERS = "SELECT member_id, name, email, phone_number FROM MEMBER";
    public static final String SELECT_USAGE_BY_MEMBER =
        """
        SELECT i.category, i.name , u.amount
        FROM USAGE_HISTORY u
        JOIN ITEM i ON u.item_id = i.item_id
        WHERE u.member_id = ?
          AND u.usage_date >= ?
          AND u.usage_date < ?
        """;
    public static final String INSERT_BILLING =
        """
        INSERT INTO BILLING (
            billing_id,
            member_id,
            billing_month,
            total_amount,
            created_at
        )
        VALUES (?, ?, ?, ?, ?)
        
        """;
    public static final String INSERT_BILLING_ITEM =
        """
        INSERT INTO BILLING_ITEM (
            billing_id,
            category,
            item_name,
            amount
        )
        VALUES (?, ?, ?, ?)
        
        """;

    // 폰 번호, 방해금지여부, 금지 시작 시간, 금지 종료 시간 추가 조회 필요
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
        FROM MEMBER m
        JOIN USAGE_HISTORY u ON m.member_id = u.member_id
        JOIN ITEM i ON u.item_id = i.item_id
        WHERE u.usage_date >= ?
        AND u.usage_date < ?
        ORDER BY m.member_id;
            
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
        template_code
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
""";
}
