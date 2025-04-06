package com.example.mssqll;

import com.example.mssqll.models.ConnectionFee;
import com.example.mssqll.models.ExtractionTask;
import com.example.mssqll.models.FileStatus;
import com.example.mssqll.models.Status;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.example.mssqll.providers.SessionProvider;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.SelectionQuery;
import org.hibernate.stat.Statistics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@SpringBootApplication
public class MssqllApplication {
        public static void main(String[] args) {
        SpringApplication.run(MssqllApplication.class, args);
    }
//    public static void main(String[] args) {
//        saveData();
//        System.out.println("Save operation completed");
//
//        enableSessionStatistics();
//        retrieveEmployee();
//        printCacheStatistics();
//        retrievePerson();
//        printCacheStatistics();
//        System.out.println("Retrieve operation completed");
//        System.out.println();
//
//        enableSessionStatistics();
//        retrieveEmployeeWithQuery();
//        printQueryCacheStatistics();
//        retrievePersonWithQuery();
//        printQueryCacheStatistics();
//        System.out.println("Retrieve with query operation completed");
//        System.out.println();
//
//
//        SessionProvider.closeSessionFactory();
//    }
//
//    private static void saveData() {
//        Session session = SessionProvider.openSession();
//        Transaction tx = session.beginTransaction();
//        try {
//            ConnectionFee employee = new ConnectionFee(
//                    "Purpose Example",
//                    1500.00,
//                    LocalDate.of(2025, 4, 6),
//                    Status.TRANSFERRED,
//                    LocalDateTime.of(2025, 4, 6, 10, 30),
//                    new ExtractionTask(
//                            LocalDateTime.of(2025, 4, 6, 10, 30),
//                            "cache",
//                            FileStatus.GOOD
//                    ),
//                    "Description Example",
//                    12345L
//            );
//            session.persist(employee);
//            tx.commit();
//        } catch (Exception e) {
//            tx.rollback();
//        } finally {
//            SessionProvider.closeSession(session);
//        }
//    }
//
//    private static void retrieveEmployee() {
//        Session session1 = SessionProvider.openSession();
//        Session session2 = SessionProvider.openSession();
//        ConnectionFee employee1 = session1.get(ConnectionFee.class, 1L);  // Fetch from DB
//        System.out.println("First Session Fetch: " + employee1);
//        ConnectionFee employee2 = session2.get(ConnectionFee.class, 1L);  // Won't hit cache
//        System.out.println("Second Session Fetch (from cache): " + employee2);
//        SessionProvider.closeSession(session1, session2);
//    }
//
//    private static void retrievePerson() {
//        Session session1 = SessionProvider.openSession();
//        Session session2 = SessionProvider.openSession();
//        ConnectionFee person1 = session1.get(ConnectionFee.class, 1L);  // Fetch from DB
//        System.out.println("First Session Fetch: " + person1);
//        ConnectionFee person2 = session2.get(ConnectionFee.class, 1L);  // Should hit cache
//        System.out.println("Second Session Fetch (from cache): " + person2);
//        SessionProvider.closeSession(session1, session2);
//
//    }
//
//    private static void retrieveEmployeeWithQuery() {
//        Session session1 = SessionProvider.openSession();
//        Session session2 = SessionProvider.openSession();
//        SelectionQuery<ConnectionFee> query1 = session1.createSelectionQuery("from ConnectionFee where id = :id", ConnectionFee.class)
//                .setParameter("id", 1L)
//                .setCacheable(true)
//                .setCacheRegion("queryCache");
//
//        ConnectionFee employee1 = query1.getSingleResult();
//        System.out.println("First Session Fetch: " + employee1);
//
//        SelectionQuery<ConnectionFee> query2 = session2.createSelectionQuery("from ConnectionFee where id = :id", ConnectionFee.class)
//                .setParameter("id", 1L)
//                .setCacheable(true)
//                .setCacheRegion("queryCache");
//        ConnectionFee employee2 = query2.getSingleResult();  // Should hit cache
//        System.out.println("Second Session Fetch (from cache): " + employee2);
//        SessionProvider.closeSession(session1, session2);
//    }
//
//    private static void retrievePersonWithQuery() {
//        Session session1 = SessionProvider.openSession();
//        Session session2 = SessionProvider.openSession();
//        SelectionQuery<ConnectionFee> query1 = session1.createSelectionQuery("from ConnectionFee where id = :id", ConnectionFee.class)
//                .setParameter("id", 1L)
//                .setCacheable(true)
//                .setCacheRegion("queryCache");
//
//        ConnectionFee person1 = query1.getSingleResult();  // Fetch from DB
//        System.out.println("First Session Fetch: " + person1);
//
//        SelectionQuery<ConnectionFee> query2 = session2.createSelectionQuery("from ConnectionFee where id = :id", ConnectionFee.class)
//                .setParameter("id", 1L)
//                .setCacheable(true)
//                .setCacheRegion("queryCache");
//
//        ConnectionFee person2 = query2.getSingleResult();  // Should hit cache
//        System.out.println("Second Session Fetch (from cache): " + person2);
//        SessionProvider.closeSession(session1, session2);
//    }
//
//    private static void enableSessionStatistics() {
//        Statistics stats = SessionProvider.getSessionFactoryStatistics();
//        stats.setStatisticsEnabled(true);
//    }
//
//    private static void printCacheStatistics() {
//        Statistics stats = SessionProvider.getSessionFactoryStatistics();
//        System.out.println("Second level cache hit count: " + stats.getSecondLevelCacheHitCount());
//        System.out.println("Second level cache miss count: " + stats.getSecondLevelCacheMissCount());
//    }
//
//    private static void printQueryCacheStatistics() {
//        Statistics stats = SessionProvider.getSessionFactoryStatistics();
//        System.out.println("Query cache hit count: " + stats.getQueryCacheHitCount());
//        System.out.println("Query cache miss count: " + stats.getQueryCacheMissCount());
//    }

}
