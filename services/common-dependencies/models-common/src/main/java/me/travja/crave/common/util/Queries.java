package me.travja.crave.common.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Queries {

//    private static final QueryBuilder.Joiner
//            joinDetails  = new QueryBuilder.Joiner("item_details", "d", "d.item_id = i.id"),
//            joinStore    = new QueryBuilder.Joiner("store", "s", "d.store_id = s.id"),
//            joinLocation = new QueryBuilder.Joiner("location", "l", "s.location_id = l.id");

//    public static final String queryNameStoreDistance = QueryBuilder.from("item", "i")
//            .select(
//                    "i.id as id",
//                    "min(i.name) as name",
//                    "(7917.5 * asin(" +
//                            "      sqrt(pow(sin(radians((min(l.lat) - :lat) / 2)), 2) +" +
//                            "        pow(sin(radians((min(l.lon) - :lon) / 2)), 2) *" +
//                            "        cos(radians(min(l.lat))) * cos(radians(:lat))" +
//                            "      )" +
//                            "    )) as distance"
//            )
//            .join(joinDetails, joinStore, joinLocation)
//            .where(NAME_LIKE).and(STORE_LIKE).end()
//            .group("i.id")
//            .having("distance < :distance").end()
//            .order(NAME_ASC)
//            .limit(":limit")
//            .offset(":offset")
//            .build();
//
//    public static final String queryNameStoreIdDistance = QueryBuilder.from("item", "i")
//            .select(
//                    "i.id as id",
//                    "min(i.name) as name",
//                    "(7917.5 * asin(" +
//                            "      sqrt(pow(sin(radians((min(l.lat) - :lat) / 2)), 2) +" +
//                            "        pow(sin(radians((min(l.lon) - :lon) / 2)), 2) *" +
//                            "        cos(radians(min(l.lat))) * cos(radians(:lat))" +
//                            "      )" +
//                            "    )) as distance"
//            )
//            .join(joinDetails, joinStore, joinLocation)
//            .where(NAME_LIKE).and(STORE_ID_EQUALS).end()
//            .group("i.id")
//            .having("distance < :distance").end()
//            .order(NAME_ASC)
//            .limit(":limit")
//            .offset(":offset")
//            .build();
//
//    public static final String countNameStoreDistance = QueryBuilder.from("(" +
//                    queryNameStoreDistance
//                    + ")", "r")
//            .select("count(distinct id)")
//            .build();
//
//
//    public static final String countNameStoreIdDistance = QueryBuilder.from("(" +
//                    queryNameStoreIdDistance
//                    + ")", "r")
//            .select("count(distinct id)")
//            .build();

    public static class Count {
        public static final String byNameStoreDistance =
                """
                select count(distinct id)
                from (select i.id as id,
                  min(i.name) as name,
                  (7917.5 * asin(
                    sqrt(pow(sin(radians((min(l.lat) - :lat) / 2)), 2) +
                      pow(sin(radians((min(l.lon) - :lon) / 2)), 2) *
                      cos(radians(min(l.lat))) * cos(radians(:lat))
                    )
                  )) as distance,
                  regexp_like(group_concat(s.name), :storeName) names
                  from item i
                    left outer join item_details d on d.item_id = i.id
                    left outer join store s on s.id = d.store_id
                    left join location l on l.id = s.location_id
                  where i.name like :name
                  group by i.id
                  having distance < :distance and names > 0
                ) r
                """,
                byNameStore                            =
                        """
                        select count(distinct id)
                        from (select i.id as id,
                          min(i.name) as name,
                          regexp_like(group_concat(s.name), :storeName) names
                          from item i
                            left outer join item_details d on d.item_id = i.id
                            left outer join store s on s.id = d.store_id
                          where i.name like :name
                          group by i.id
                          having names > 0
                        ) r
                        """;
    }

    public static class Ids {

        public static class ByName {
            /**
             * Get by name and store name and distance, sorted by name
             */
            public static final String byNameStoreDistance =
                    """
                    select distinct id
                    from (select i.id as id, min(i.name) as name,
                      (7917.5 * asin(
                        sqrt(pow(sin(radians((min(l.lat) - :lat) / 2)), 2) +
                          pow(sin(radians((min(l.lon) - :lon) / 2)), 2) *
                          cos(radians(min(l.lat))) * cos(radians(:lat))
                        )
                      )) as distance,
                      regexp_like(group_concat(s.name), :storeName) names
                      from item i
                       left outer join item_details d on d.item_id = i.id
                       left outer join store s on s.id = d.store_id
                       left join location l on l.id = s.location_id
                      where i.name like :name
                      group by i.id
                      having distance < :distance and names > 0
                      order by name asc
                      limit :limit offset :offset
                    ) r
                    """,

            /**
             * Get by name and store id, sorted by name
             */
            byNameStoreId =
                    """
                    select distinct id
                    from (select i.id as id, min(i.name) as name
                      from item i
                       left outer join item_details d on d.item_id = i.id
                       left outer join store s on s.id = d.store_id
                     where i.name like :name
                       and s.id = :storeId
                     group by i.id
                     order by name asc
                     limit :limit offset :offset
                    ) r
                    """,

            /**
             * Get by name and store name, sorted by name
             */
            byNameStore =
                    """
                    select distinct id
                    from (select i.id as id, min(i.name) as name
                      from item i
                       left outer join item_details d on d.item_id = i.id
                       left outer join store s on s.id = d.store_id
                      where i.name like :name
                        and regexp_like(s.name, :storeName) > 0
                      group by i.id
                      order by name asc
                      limit :limit offset :offset
                    ) r
                    """;
        }

        public static class ByLowest {
            public static final String
                    byNameAndStore      =
                    """
                    select distinct id from (
                      select i.id, min(i.name) as name, min(s2.new_price) as sale, min(d.price) as min
                      from item i
                        left outer join item_details d on d.item_id = i.id
                        left outer join store s on s.id = d.store_id
                        left outer join sale s2 on d.id = s2.item_id
                      where i.name like :name
                        and regexp_like(s.name, :storeName) > 0
                        and (s2.new_price is null or s2.end_date > CURRENT_DATE)
                      group by i.id
                      order by sale asc, min asc, name asc
                      limit :limit
                      offset :offset
                    ) r
                    """,
                    byNameAndStoreId    =
                            """
                            select distinct id
                            from (
                              select i.id, min(i.name) as name, min(s2.new_price) as sale, min(d.price) as min
                              from item i
                                left outer join item_details d on d.item_id = i.id
                                left outer join store s on s.id = d.store_id
                                left outer join sale s2 on d.id = s2.item_id
                              where i.name like :name
                                and s.id like :storeId
                                and (s2.new_price is null or s2.end_date > CURRENT_DATE)
                              group by i.id
                              order by sale asc, min asc, name asc
                              limit :limit
                              offset :offset
                            ) r
                            """,
                    byNameStoreDistance =
                            """
                            select distinct id
                            from (select i.id, min(i.name) as name, min(s2.new_price) as sale, min(d.price) as min,
                              (7917.5 * asin(
                                sqrt(pow(sin(radians((min(l.lat) - :lat) / 2)), 2) +
                                  pow(sin(radians((min(l.lon) - :lon) / 2)), 2) *
                                  cos(radians(min(l.lat))) * cos(radians(:lat))
                                )
                              )) as distance,
                              regexp_like(group_concat(s.name), :storeName) names
                              from item i
                               left outer join item_details d on d.item_id = i.id
                               left outer join store s on s.id = d.store_id
                               left outer join sale s2 on d.id = s2.item_id
                               left join location l on l.id = s.location_id
                              where i.name like :name
                              group by i.id
                              having distance < :distance and names > 0
                              order by sale asc, min asc, name asc
                              limit :limit offset :offset
                            ) r
                            """;
        }

        public static class ByHighest {
            public static final String
                    byNameandStore      =
                    """
                    select distinct id from (
                      select i.id, min(i.name) as name, min(s2.new_price) as sale, min(d.price) as min
                      from item i
                        left outer join item_details d on d.item_id = i.id
                        left outer join store s on s.id = d.store_id
                        left outer join sale s2 on d.id = s2.item_id
                      where i.name like :name
                        and regexp_like(s.name, :storeName) > 0
                        and (s2.new_price is null or s2.end_date > CURRENT_DATE)
                      group by i.id
                      order by sale desc, min desc, name asc
                      limit :limit
                      offset :offset
                    ) r
                    """,
                    byNameAndStoreId    =
                            """
                            select distinct id
                            from (
                              select i.id, min(i.name) as name, min(s2.new_price) as sale, min(d.price) as min
                              from item i
                                left outer join item_details d on d.item_id = i.id
                                left outer join store s on s.id = d.store_id
                                left outer join sale s2 on d.id = s2.item_id
                              where i.name like :name
                                and s.id like :storeId
                                and (s2.new_price is null or s2.end_date > CURRENT_DATE)
                              group by i.id
                              order by sale desc, min desc, name asc
                              limit :limit
                              offset :offset
                            ) r
                            """,
                    byNameStoreDistance =
                            """
                            select distinct id
                            from (select i.id, min(i.name) as name, min(s2.new_price) as sale, min(d.price) as min,
                              (7917.5 * asin(
                                sqrt(pow(sin(radians((min(l.lat) - :lat) / 2)), 2) +
                                  pow(sin(radians((min(l.lon) - :lon) / 2)), 2) *
                                  cos(radians(min(l.lat))) * cos(radians(:lat))
                                )
                              )) as distance,
                              regexp_like(group_concat(s.name), :storeName) names
                              from item i
                               left outer join item_details d on d.item_id = i.id
                               left outer join store s on s.id = d.store_id
                               left outer join sale s2 on d.id = s2.item_id
                               left join location l on l.id = s.location_id
                              where i.name like :name
                              group by i.id
                              having distance < :distance and names > 0
                              order by sale desc, min desc, name asc
                              limit :limit offset :offset
                            ) r
                            """;
        }

    }

    @Data
    public static class QueryBuilder {

        public String       from   = "";
        public List<String> select = new LinkedList<>();
        public List<String> join   = new LinkedList<>();
        public List<String> where  = new LinkedList<>();
        public List<String> group  = new LinkedList<>();
        public List<String> having = new LinkedList<>();
        public List<String> order  = new LinkedList<>();
        public String       limit  = "", offset = "";

        public static QueryBuilder from(String table, String alias) {
            QueryBuilder qb = new QueryBuilder();
            qb.from = "from " + table + " " + alias;
            return qb;
        }

        public QueryBuilder select(String... list) {
            select.addAll(List.of(list));
            return this;
        }

        /**
         * Creates a left outer join parameter for the query.
         *
         * @param join  - The table to join
         * @param alias - The alias for the table
         * @param on    - The on condition, ie: s.id = d.sale_id
         * @return The {@link QueryBuilder} object
         */
        public QueryBuilder join(String join, String alias, String on) {
            this.join.add("left outer join " + join + " " + alias + " on " + on);

            return this;
        }

        public QueryBuilder join(Joiner... joiners) {
            this.join.addAll(Arrays.stream(joiners)
                    .map(joiner -> "left outer join " + joiner.getTable() + " " + joiner.getAlias() + " on " + joiner.getOn())
                    .collect(Collectors.toList()));
            return this;
        }

        /**
         * Creates a where condition. Defined without 'where'
         *
         * @param where - In the format `col = 'foobar'`
         * @return QueryBuilder
         */
        public ConditionBuilder where(String where) {
            return new ConditionBuilder(this, this.where, where);
        }

        private QueryBuilder where(List<String> conditions) {
            this.where.addAll(conditions);
            return this;
        }

        public QueryBuilder group(String... group) {
            this.group.addAll(List.of(group));
            return this;
        }

        public ConditionBuilder having(String condition) {
            return new ConditionBuilder(this, this.having, condition);
        }


        /**
         * Adds order parameters to the query.
         *
         * @param order - like `i.name asc`
         * @return QueryBuilder
         */
        public QueryBuilder order(String... order) {
            this.order.addAll(List.of(order));
            return this;
        }

        public QueryBuilder limit(String lim) {
            this.limit = lim;
            return this;
        }

        public QueryBuilder offset(String offset) {
            this.offset = offset;
            return this;
        }

        /**
         * Builds the query as was defined by the various stages of the
         * calling stack.
         *
         * @return The query as a String
         */
        public String build() {
            String query = "select " + String.join(", ", select) + from;

            if (!join.isEmpty())
                query += " " + String.join(" ", join);

            if (!where.isEmpty())
                query += " where " + String.join(" ", where);

            if (!group.isEmpty())
                query += " group by " + String.join(", ", group);

            if (!having.isEmpty())
                query += " having " + String.join(" ", having);

            if (!order.isEmpty())
                query += " order by " + String.join(", ", order);

            if (!limit.trim().isEmpty())
                query += " limit " + limit;

            if (!offset.trim().isEmpty())
                query += " offset " + offset;

            return query;
        }

        @Data
        public static class ConditionBuilder {

            public QueryBuilder parent;
            public List<String> destination;
            public List<String> conditions = new LinkedList<>();

            public ConditionBuilder(QueryBuilder parent, List<String> destination, String baseCondition) {
                this.parent = parent;
                this.destination = destination;
                conditions.add(baseCondition);
            }

            public ConditionBuilder or(String condition) {
                conditions.add(condition);
                return this;
            }

            public ConditionBuilder and(String condition) {
                conditions.add(condition);
                return this;
            }

            public QueryBuilder end() {
                if (destination != null)
                    destination.addAll(conditions);
                return parent;
            }

        }

        @Data
        @AllArgsConstructor
        public static class Joiner {
            private String table, alias, on;
        }

        public enum Operator {
            AND,
            OR
        }

    }

}
