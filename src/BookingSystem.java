import java.time.LocalDate;
import java.util.*;

public class BookingSystem {
    private static ControllerImplementation controller = new ControllerImplementation();
    private static Boolean canReserve = false;
    private static Boolean canConfirm = false;
    private static Booking newBooking;


    private static String setAccommodationFilteringData(Scanner scanner){
        System.out.print("Ciudad: ");
        String city = scanner.nextLine();
        System.out.println("Tipo de alojamiento:");
        System.out.println("1. Hotel");
        System.out.println("2. Apartamento");
        System.out.println("3. Finca");
        System.out.println("4. Día de sol");
        System.out.print("Seleccione una opción: ");
        Integer selectedType = scanner.nextInt();
        String type = "";
        switch (selectedType) {
            case 1 -> type = "Hotel";
            case 2 -> type = "Apartment";
            case 3 -> type = "Farm";
            case 4 -> type = "Sunny Day";
        }
        Integer start;
        Integer end = 0;
        Integer roomQuantity = 0;
        if(type.equals("Sunny Day")){
            System.out.print("Fecha: ");
            start = scanner.nextInt();
        }
        else{
            System.out.print("Fecha de inicio: ");
            start = scanner.nextInt();
            System.out.print("Fecha de fin: ");
            end = scanner.nextInt();
            System.out.print("Cantidad de habitaciones: ");
            roomQuantity = scanner.nextInt();
        }
        System.out.print("Cantidad de adultos: ");
        Integer adultsQuantity = scanner.nextInt();
        System.out.print("Cantidad de niños: ");
        Integer childrenQuantity = scanner.nextInt();
        scanner.nextLine();
        newBooking = new Booking(start,end,adultsQuantity,childrenQuantity,roomQuantity,type,city);
        return type;
    }


    public static void showFilteredAccommodations(Scanner scanner, String type){
        controller.filterAccommodations(newBooking);
        ArrayList<Accommodation> selectedAccommodations = controller.getSelectedAccommodations();

        if (selectedAccommodations.isEmpty()) {
            System.out.println("No se encontraron alojamientos disponibles según los criterios ingresados.");
        } else {
            System.out.println("\nAlojamientos encontrados:\n");
            int number = 1;
            for (Accommodation accommodation : selectedAccommodations) {
                System.out.println(number+". "+ accommodation.getName());
                System.out.println(accommodation.showAccommodation(newBooking));
                number++;
            }
            System.out.print("Seleccione una opción: ");
            int accOption = scanner.nextInt();
            if(accOption > 0 && accOption <= selectedAccommodations.size()){
                Accommodation acc= selectedAccommodations.get(accOption-1);
                newBooking.setAccommodation(acc.getName());
                if(type.equals("Hotel")){
                    canConfirm = true;
                }
                else{
                    newBooking.setFinalPrice(acc.getPriceDetail().getFinalPrice());
                    canReserve = true;
                }
                System.out.println("Alojamiento seleccionado: "+acc.getName());
            }
            else{
                newBooking = null;
                System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
    }

    public static void reserveAccommodation(Scanner scanner){
        if(canReserve){
            System.out.print("Nombre: ");
            String firstName = scanner.nextLine();
            System.out.print("Apellido: ");
            String lastName = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Nacionalidad: ");
            String nationality = scanner.nextLine();
            System.out.print("Teléfono: ");
            String phone = scanner.nextLine();
            System.out.print("Fecha de nacimiento (YYYY-MM-DD): ");
            String birthDate = scanner.nextLine();
            System.out.print("Hora aproximada de llegada (HH:mm): ");
            String time = scanner.nextLine();

            Customer customer = new Customer(firstName,lastName,email,nationality,phone, LocalDate.parse(birthDate));
            boolean reserved = controller.reserve(customer, time, newBooking);
            if(reserved){
                System.out.println("Se ha realizado la reserva con éxito.");
            }
            else{
                System.out.println("Error al intentar reservar.");
            }
            canReserve = false;
            newBooking = null;
        }
        else{
            System.out.println("No es posible reservar.");
        }
    }

    public static void showRoomOptions(Booking booking, Hotel hotel, ArrayList<Integer> availableRooms){
        int number = 1;
        for( RoomModel model:hotel.getRoomModels()){
            System.out.println(number+". "+model.toString());
            System.out.println("Habitaciones disponibles: "+availableRooms.get(number-1));
            hotel.setRoomModelIndex(number-1);
            System.out.println(hotel.showAccommodation(booking));
            number++;
        }
        hotel.setRoomModelIndex(0);
    }

    public static void setBooking(Hotel hotel,Integer roomOption, Booking booking){
        booking.setRoomModel(hotel.getRoomModels().get(roomOption-1));
        hotel.setRoomModelIndex(roomOption-1);
        hotel.calculateStayPrice(booking);
        booking.setFinalPrice(hotel.getPriceDetail().getFinalPrice());
    }

    public static void selectRoom(Booking booking, Scanner scanner, Boolean isUpdate){
        Hotel hotel = controller.getHotel(booking.getAccommodation());
        ArrayList<Integer> availableRooms = controller.confirmRooms(hotel, booking);
        showRoomOptions(booking,hotel,availableRooms);
        if(isUpdate){
            System.out.println("Tiene habitaciones: "+ booking.getRoomModel().getTitle());
        }
        System.out.print("Seleccione una opción: ");
        int roomOption = scanner.nextInt();
        if(roomOption > 0 && roomOption <= availableRooms.size()){
            if(availableRooms.get(roomOption-1) >= booking.getRoomQuantity() ){
                setBooking(hotel,roomOption, booking);
                if(!isUpdate){
                    canConfirm = false;
                    canReserve = true;
                }
            }
            else{
                System.out.println("La cantidad de habitaciones para el tipo elegido es insuficiente.");
            }
        }
        else{
            if(!isUpdate){
                booking = null;
                canConfirm = false;
            }
            System.out.println("Opción inválida. Intente de nuevo.");
        }
        hotel.setRoomModelIndex(0);
    }

    public static void updateAccommodation(Scanner scanner){
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Fecha de nacimiento: ");
        String birthDate = scanner.nextLine();

        ArrayList<Booking> validBookings = controller.validateUser(email,birthDate);

        if(!validBookings.isEmpty()){
            Integer number = 1;
            for(Booking booking:validBookings){
                System.out.println(number+". "+booking+"\n");
                number++;
            }
            System.out.print("Seleccione una opción: ");
            int bookingOption = scanner.nextInt();
            Booking selecteBooking = validBookings.get(bookingOption-1);

            if(selecteBooking.getType().equals("Hotel")){
                System.out.println("1. Cambio de alojameinto");
                System.out.println("2. Cambio de habitación");
            }
            else{
                System.out.println("1. Cambio de alojamiento");
            }
            System.out.print("Seleccione una opción: ");
            int changeOption = scanner.nextInt();
            if(changeOption == 1){
                controller.getBookings().remove(selecteBooking);
                System.out.print("Escoja un nuevo alojamiento.");
            } else if (changeOption == 2) {
                selectRoom(selecteBooking,scanner, true);
            }
        }
        else {
            System.out.println("Usuario inválido");
        }
    }

    public static Integer mainMenu(Scanner scanner){
        System.out.println("\nSistema de Reservas");
        System.out.println("1. Buscar alojamiento");
        System.out.println("2. Confirmar habitaciones (hoteles)");
        System.out.println("3. Realizar reserva");
        System.out.println("4. Actualizar reserva");
        System.out.println("0. Salir\n");
        System.out.print("Seleccione una opción: ");
        Integer opcion = scanner.nextInt();
        scanner.nextLine();
        return opcion;
    }

    public static void main(String[] args) {
        controller.loadData();
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            Integer opcion =  mainMenu(scanner);
            switch (opcion) {
                case 1 -> {
                    String type = setAccommodationFilteringData(scanner);
                    showFilteredAccommodations(scanner, type);
                }
                case 2 ->{
                    if(canConfirm){
                        selectRoom(newBooking,scanner, false);
                    }
                    else{
                        System.out.println("No es posible confirmar habitaciones de hotel.");
                    }
                }
                case 3 ->{
                    reserveAccommodation(scanner);
                }
                case 4 ->{
                    updateAccommodation(scanner);
                }
                case 0 -> {
                    System.out.println("Gracias por usar el sistema. Adiós!");
                    exit = true;
                }
                default -> System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
        scanner.close();
    }
}