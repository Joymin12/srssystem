package com.se03;

import com.se03.service.ClassroomManagementService;
import com.se03.service.RoomStatusService;
import com.se03.service.NotificationService;
import com.se03.service.ReservationService;
import com.se03.service.AuthService;
import com.se03.repository.FileClassroomRepository;
import com.se03.repository.FileDatabase;
import com.se03.repository.FileLectureScheduleRepository;
import com.se03.repository.FileNotificationRepository;
import com.se03.repository.FileReservationRepository;
import com.se03.repository.FileUserRepository;
import com.se03.controller.ApprovalController;
import com.se03.handler.ApprovalHandler;
import com.se03.controller.AvailableRoomController;
import com.se03.handler.AvailableRoomHandler;
import com.se03.handler.CancelReservationHandler;
import com.se03.support.HttpSupport;
import com.se03.controller.ManagementController;
import com.se03.handler.ManagementHandler;
import com.se03.controller.NotificationController;
import com.se03.handler.NotificationHandler;
import com.se03.controller.ReservationController;
import com.se03.handler.ReservationHandler;
import com.se03.controller.RoomStatusController;
import com.se03.handler.RoomStatusHandler;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.Executors;

public class Main {
    private static final int PORT = Integer.parseInt(System.getProperty("srs.port", "8080"));

    public static void main(String[] args) throws Exception {
        FileDatabase database = new FileDatabase(Path.of("data", "store.tsv"));
        FileUserRepository userRepository = new FileUserRepository(database);
        FileClassroomRepository classroomRepository = new FileClassroomRepository(database);
        FileLectureScheduleRepository lectureScheduleRepository = new FileLectureScheduleRepository(database);
        FileReservationRepository reservationRepository = new FileReservationRepository(database);
        FileNotificationRepository notificationRepository = new FileNotificationRepository(database);

        AuthService authService = new AuthService(userRepository);
        NotificationService notificationService = new NotificationService(notificationRepository);
        RoomStatusService roomStatusService = new RoomStatusService(classroomRepository, lectureScheduleRepository, reservationRepository);
        ReservationService reservationService = new ReservationService(classroomRepository, lectureScheduleRepository, reservationRepository, notificationService, userRepository);
        ClassroomManagementService managementService = new ClassroomManagementService(classroomRepository, lectureScheduleRepository, reservationRepository, userRepository);

        RoomStatusController roomStatusController = new RoomStatusController(roomStatusService);
        AvailableRoomController availableRoomController = new AvailableRoomController(reservationService);
        ReservationController reservationController = new ReservationController(reservationService);
        ApprovalController approvalController = new ApprovalController(reservationService);
        ManagementController managementController = new ManagementController(managementService);
        NotificationController notificationController = new NotificationController(notificationService);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/rooms/available", new AvailableRoomHandler(availableRoomController));
        server.createContext("/rooms", new RoomStatusHandler(roomStatusController));
        server.createContext("/reservations/pending", new ApprovalHandler(approvalController));
        server.createContext("/reservations", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.matches("/reservations/[^/]+/cancel")) new CancelReservationHandler(reservationController).handle(exchange);
            else if (path.matches("/reservations/[^/]+/force-cancel")) new CancelReservationHandler(reservationController).handle(exchange);
            else if (path.matches("/reservations/[^/]+/(approve|reject)")) new ApprovalHandler(approvalController).handle(exchange);
            else new ReservationHandler(reservationController).handle(exchange);
        });
        server.createContext("/notifications", new NotificationHandler(notificationController));
        server.createContext("/admin/classrooms", new ManagementHandler(managementController));
        server.createContext("/admin/schedules", new ManagementHandler(managementController));
        server.createContext("/login", exchange -> {
            var p = HttpSupport.params(exchange);
            var user = authService.login(p.get("userId"), p.get("password"));
            String body = user == null ? "ERROR\nmessage=로그인 실패\n" : "OK\nuserId=" + user.userId() + "\nname=" + user.name() + "\nrole=" + user.role() + "\n";
            HttpSupport.respond(exchange, body);
        });
        server.createContext("/backup", exchange -> {
            String body = database.backup(Path.of("data", "backup.yml"));
            HttpSupport.respond(exchange, body);
        });
        server.createContext("/restore", exchange -> {
            String body = database.restore(Path.of("data", "backup.yml"));
            HttpSupport.respond(exchange, body);
        });
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("SRS server started: http://localhost:" + PORT);
    }
}
