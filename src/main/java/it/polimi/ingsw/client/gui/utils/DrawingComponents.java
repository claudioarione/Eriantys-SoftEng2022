package it.polimi.ingsw.client.gui.utils;

import it.polimi.ingsw.constants.Constants;
import it.polimi.ingsw.constants.Messages;
import it.polimi.ingsw.model.characters.CharacterName;
import it.polimi.ingsw.model.game_objects.Color;
import it.polimi.ingsw.model.game_objects.Student;
import it.polimi.ingsw.server.game_state.*;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DrawingComponents {

    private static double dashboardHeight;
    private static final List<BorderPane> characterImages = new ArrayList<>();
    private static final List<AnchorPane> cloudImages = new ArrayList<>();
    private static final List<BorderPane> assistantCards = new ArrayList<>();
    private static final List<BorderPane> entranceStudents = new ArrayList<>();
    private static final List<BorderPane> diningGaps = new ArrayList<>();
    private static final List<BorderPane> diningStudents = new ArrayList<>();
    private static final HashMap<CharacterName, List<BorderPane>> studentsOnCharacter = new HashMap<>();
    public static final HashMap<Integer, List<BorderPane>> studentsOnIslands = new HashMap<>();
    public static final List<BorderPane> noEntriesOnIslands = new ArrayList<>();
    private static final List<BorderPane> islands = new ArrayList<>();
    private static List<String> lastActions;

    /**
     * Draws the given {@code GameState} of a two players {@code Game}
     *
     * @param gameState  the given {@code GameState} to be drawn
     * @param pageWidth  the width of the screen
     * @param pageHeight the height of the screen
     * @param root       the {@code AnchorPane} to attach the different components to
     * @param username   the username of the {@code Player} whose GUI will be drawn the given {@code GameState} on
     */
    public static void drawTwoPlayersGame(GameState gameState, double pageWidth, double pageHeight, AnchorPane root, String username) {
        drawGameComponentsForTwo(pageWidth, pageHeight, root, gameState, username);

        drawAssistants(gameState, pageWidth, pageHeight, root, username);
    }

    /**
     * Draws the given {@code GameState} of a three players {@code Game}
     *
     * @param gameState  the given {@code GameState} to be drawn
     * @param pageWidth  the width of the screen
     * @param pageHeight the height of the screen
     * @param root       the {@code AnchorPane} to attach the different components to
     * @param username   the username of the {@code Player} whose GUI will be drawn the given {@code GameState} on
     */
    public static void drawThreePlayersGame(GameState gameState, double pageWidth, double pageHeight, AnchorPane root, String username) {
        List<PlayerState> players = gameState.getPlayers();
        drawGameComponentsForTwo(pageWidth, pageHeight, root, gameState, username);

        drawDashboard(players.get(2), pageHeight * DrawingConstants.THIRD_DASHBOARD_Y, root, username);
        drawDashboardText(players.get(2), 0 - pageWidth * DrawingConstants.XOFFSET_DASH_TEXT, 3 * dashboardHeight + pageHeight * DrawingConstants.LOWER_YOFFSET_DASH_TEXT, pageWidth, pageHeight, root, gameState.isExpert());

        drawAssistants(gameState, pageWidth, pageHeight, root, username);
    }

    /**
     * Draws the given {@code GameState} of a four players {@code Game}
     *
     * @param gameState  the given {@code GameState} to be drawn
     * @param pageWidth  the width of the screen
     * @param pageHeight the height of the screen
     * @param root       the {@code AnchorPane} to attach the different components to
     * @param username   the username of the {@code Player} whose GUI will be drawn the given {@code GameState} on
     */
    public static void drawFourPlayersGame(GameState gameState, double pageWidth, double pageHeight, AnchorPane root, String username) {
        List<PlayerState> players = gameState.getPlayers();
        drawGameComponentsForTwo(pageWidth, pageHeight, root, gameState, username);

        drawDashboard(players.get(2), pageHeight * DrawingConstants.THIRD_DASHBOARD_Y, root, username);
        drawDashboardText(players.get(2), 0 - pageWidth * DrawingConstants.XOFFSET_DASH_TEXT, 3 * dashboardHeight + pageHeight * DrawingConstants.LOWER_YOFFSET_DASH_TEXT, pageWidth, pageHeight, root, gameState.isExpert());

        drawDashboard(players.get(3), pageHeight * DrawingConstants.FOURTH_DASHBOARD_Y, root, username);
        drawDashboardText(players.get(3), dashboardHeight / (2 * DrawingConstants.DASHBOARD_HEIGHT_OVER_WIDTH) - pageWidth * DrawingConstants.XOFFSET_DASH_TEXT, 3 * dashboardHeight + pageHeight * DrawingConstants.LOWER_YOFFSET_DASH_TEXT, pageWidth, pageHeight, root, gameState.isExpert());

        drawAssistants(gameState, pageWidth, pageHeight, root, username);
    }

    /**
     * Draws the components of a two players {@code Game}
     *
     * @param pageWidth  the width of the screen
     * @param pageHeight the height of the screen
     * @param root       the {@code AnchorPane} to attach the different components to
     * @param gameState  the given {@code GameState} to be drawn
     * @param username   the username of the {@code Player} whose GUI will be drawn the given {@code GameState} on
     */
    private static void drawGameComponentsForTwo(double pageWidth, double pageHeight, AnchorPane root, GameState gameState, String username) {
        root.getStylesheets().add("/css/game_elements.css");

        dashboardHeight = pageHeight * DrawingConstants.DASHBOARD_HEIGHT;
        List<PlayerState> players = gameState.getPlayers();

        drawDashboard(players.get(0), 0, root, username);
        drawDashboardText(players.get(0), 0 - pageWidth * DrawingConstants.XOFFSET_DASH_TEXT, dashboardHeight + pageHeight * DrawingConstants.UPPER_YOFFSET_DASH_TEXT, pageWidth, pageHeight, root, gameState.isExpert());

        drawDashboard(players.get(1), pageHeight * DrawingConstants.SECOND_DASHBOARD_Y, root, username);
        drawDashboardText(players.get(1), dashboardHeight / (2 * DrawingConstants.DASHBOARD_HEIGHT_OVER_WIDTH) - pageWidth * DrawingConstants.XOFFSET_DASH_TEXT, dashboardHeight + pageHeight * DrawingConstants.UPPER_YOFFSET_DASH_TEXT, pageWidth, pageHeight, root, gameState.isExpert());

        drawClouds(gameState.getClouds(), pageWidth, pageHeight, root);
        drawIslands(gameState, pageWidth, pageHeight, root);
        // Draw all three characters
        if (gameState.isExpert()) {
            drawCharacters(gameState.getCharacters(), pageWidth, pageHeight, root);
        }
    }

    /**
     * Draws a {@code DashBoard} with all the students in the {@code Entrance} and {@code DiningRoom}, the professors and the towers
     *
     * @param player   the player to draw the {@code DashBoard} of
     * @param y        the starting Y coordinate of the {@code DashBoard}
     * @param root     the {@code AnchorPane} to attach the {@code DashBoard} to
     * @param username the username of the player who owns the {@code DashBoard}
     */
    private static void drawDashboard(PlayerState player, double y, AnchorPane root, String username) {
        ImageView dashboardImage = new ImageView(new Image("/gameboard/Plancia_DEF_circles.png"));
        dashboardImage.setPreserveRatio(true);
        dashboardImage.setFitHeight(dashboardHeight);
        double dashboardWidth = dashboardImage.getBoundsInParent().getWidth();
        BorderPane dashboard = new BorderPane(dashboardImage);
        dashboard.setLayoutX(0);
        dashboard.setLayoutY(y);
        root.getChildren().add(dashboard);

        // Add students to entrance
        GridPane newEntrance = getGridPane(
                dashboardWidth * DrawingConstants.INITIAL_X_OFFSET_ENTRANCE, dashboardWidth * DrawingConstants.INITIAL_Y_OFFSET_ENTRANCE,
                dashboardWidth * DrawingConstants.ENTRANCE_HGAP, dashboardWidth * DrawingConstants.ENTRANCE_VGAP
        );
        for (int i = 0; i < player.getEntrance().size(); i++) {
            Student s = player.getEntrance().get(i);
            String resourceName = "/gameboard/students/student_" + s.getColor().toString().toLowerCase() + ".png";
            ImageView student = getImageView(resourceName, dashboardWidth / DrawingConstants.STUDENT_DIMENSION_DIVISOR);
            student.setOnMouseClicked(mouseEvent -> ObjectClickListeners.setStudentClicked(s.getColor(), student));

            BorderPane studentPane = new BorderPane(student);
            if (username.equals(player.getName())) {
                studentPane.setOnMouseClicked(event -> ObjectClickListeners.setStudentClicked(s.getColor(), studentPane));
                entranceStudents.add(studentPane);
            }

            newEntrance.add(studentPane, (i + 1) % 2, i / 2 + i % 2);
        }
        // Students in entrance are attached to the gameboard and not to the root directly
        dashboard.getChildren().add(newEntrance);

        // Add students to dining room
        GridPane newDiningRoom = getGridPane(dashboardWidth * DrawingConstants.INITIAL_X_OFFSET_DINING_ROOM,
                dashboardWidth * DrawingConstants.INITIAL_Y_OFFSET_DINING_ROOM, dashboardWidth * DrawingConstants.DINING_ROOM_HGAP,
                dashboardWidth * DrawingConstants.DINING_ROOM_VGAP);

        List<Color> colorsInOrder = List.of(Color.GREEN, Color.RED, Color.YELLOW, Color.PINK, Color.BLUE);
        HashMap<Color, Integer> diningStudents = new HashMap<>();
        for (int i = 0; i < player.getDining().size(); i++) {
            Student s = player.getDining().get(i);
            String resourceName = "/gameboard/students/student_" + s.getColor().toString().toLowerCase() + ".png";
            ImageView student = getImageView(resourceName, dashboardWidth / DrawingConstants.STUDENT_DIMENSION_DIVISOR);

            int row = colorsInOrder.indexOf(s.getColor());
            Integer column = diningStudents.get(s.getColor());
            if (column == null) {
                column = 0;
            }

            BorderPane studentWithBorder = new BorderPane(student);
            studentWithBorder.setOnMouseClicked(event -> ObjectClickListeners.setStudentOnDiningClicked(s.getColor(), studentWithBorder));
            diningStudents.put(s.getColor(), column + 1);
            newDiningRoom.add(studentWithBorder, column, row);

            if (username.equals(player.getName())) {
                DrawingComponents.diningStudents.add(studentWithBorder);
            }
        }

        for (Color color : colorsInOrder) {
            diningStudents.putIfAbsent(color, 0);
            int positionOfEmptySpace = diningStudents.get(color);
            if (positionOfEmptySpace == 10) break;

            // If you want to change to a circle, the radius is width / 25 * 0,55
            BorderPane emptySpace = new BorderPane();
            emptySpace.setOnMouseClicked(mouseEvent -> ObjectClickListeners.setDiningRoomClicked());
            emptySpace.setMaxSize(dashboardWidth / DrawingConstants.STUDENT_DIMENSION_DIVISOR, dashboardWidth / DrawingConstants.STUDENT_DIMENSION_DIVISOR);
            emptySpace.setMinSize(dashboardWidth / DrawingConstants.STUDENT_DIMENSION_DIVISOR, dashboardWidth / DrawingConstants.STUDENT_DIMENSION_DIVISOR);

            newDiningRoom.add(emptySpace, positionOfEmptySpace, colorsInOrder.indexOf(color));

            if (username.equals(player.getName())) {
                diningGaps.add(emptySpace);
            }
        }
        // Students in dining room are attached to the dashboard and not to the root directly
        dashboard.getChildren().add(newDiningRoom);

        // Add professors
        GridPane professorRoom = getGridPane(
                dashboardWidth * DrawingConstants.INITIAL_X_OFFSET_PROFESSOR_ROOM, dashboardWidth * DrawingConstants.INITIAL_Y_OFFSET_PROFESSOR_ROOM,
                0, dashboardWidth * DrawingConstants.PROFESSOR_ROOM_VGAP
        );
        for (int i = 0; i < colorsInOrder.size(); i++) {
            Color color = colorsInOrder.get(i);
            if (player.getOwnedProfessors().contains(color)) {
                String path = "/gameboard/professors/teacher_" + color.toString().toLowerCase() + ".png";
                ImageView professor = getImageView(path, dashboardWidth / DrawingConstants.PROFESSOR_DIMENSION_DIVISOR);
                professor.setRotate(DrawingConstants.PROFESSOR_ROTATION);

                professorRoom.add(professor, 0, i);
            } else {
                Pane emptyPlace = new Pane();
                emptyPlace.setMaxSize(dashboardWidth / DrawingConstants.PROFESSOR_DIMENSION_DIVISOR, dashboardWidth / DrawingConstants.PROFESSOR_DIMENSION_DIVISOR);
                emptyPlace.setMinSize(dashboardWidth / DrawingConstants.PROFESSOR_DIMENSION_DIVISOR, dashboardWidth / DrawingConstants.PROFESSOR_DIMENSION_DIVISOR);
                professorRoom.add(emptyPlace, 0, i);
            }

        }
        // Professors are attached to the gameboard and not to the root directly
        dashboard.getChildren().add(professorRoom);

        // Add towers
        GridPane towers = getGridPane(
                dashboardWidth * DrawingConstants.INITIAL_X_OFFSET_TOWERS, dashboardWidth * DrawingConstants.INITIAL_Y_OFFSET_TOWERS,
                dashboardWidth * DrawingConstants.TOWERS_HGAP, dashboardWidth * DrawingConstants.TOWERS_VGAP
        );
        for (int i = 0; i < player.getRemainingTowers(); i++) {
            String path = "/gameboard/towers/" + player.getTowerColor().toString().toLowerCase() + "_tower.png";
            ImageView tower = getImageView(path, dashboardWidth * DrawingConstants.TOWERS_SIZE);

            towers.add(tower, i % 2, i / 2);
        }
        // Towers are attached to the gameboard and not to the root directly
        dashboard.getChildren().add(towers);

    }

    /**
     * Draws the text under/over the {@code DashBoard} with the username of the given {@code Player} and, if the game is in ExpertMode, the number of coins they own
     *
     * @param player     the player to insert in the text
     * @param x          the starting X coordinate of the text
     * @param y          the starting Y coordinate of the text
     * @param pageWidth  the width of the screen
     * @param pageHeight the height of the screen
     * @param root       the {@code AnchorPane} to attach te text to
     * @param isExpert   true if the the {@code Game} is in Expert Mode
     */
    private static void drawDashboardText(PlayerState player, double x, double y, double pageWidth, double pageHeight, AnchorPane root, boolean isExpert) {
        double startingX = x + pageWidth * DrawingConstants.PLAYER_NAME_INITIAL_PADDING;
        Text text;

        if (!isExpert) {
            if (x == 0 - pageWidth * DrawingConstants.XOFFSET_DASH_TEXT) {
                text = new Text("↑ | " + player.getName());
            } else {
                text = new Text("↓ | " + player.getName());
            }
            text.setX(startingX);
            text.setY(y);
            text.setFont(Font.font(DrawingConstants.FONT_NAME, FontWeight.BOLD, DrawingConstants.TITLE_FONT_SIZE));
            root.getChildren().add(text);
        } else {
            if (x == 0 - pageWidth * DrawingConstants.XOFFSET_DASH_TEXT) {
                text = new Text("↑ | " + player.getName() + " | " + player.getNumCoins() + "x");
            } else {
                text = new Text("↓ | " + player.getName() + " | " + player.getNumCoins() + "x");
            }
            text.setX(startingX);
            text.setY(y);
            text.setFont(Font.font(DrawingConstants.FONT_NAME, FontWeight.BOLD, DrawingConstants.TITLE_FONT_SIZE));
            root.getChildren().add(text);
            ImageView coin = getCoinImageView(startingX + text.getLayoutBounds().getWidth(),
                    y - pageHeight / DrawingConstants.COIN_DIMENSION_IN_TEXT_DIVISOR, pageWidth * DrawingConstants.COIN_PROPORTION);
            root.getChildren().add(coin);
        }
    }

    /**
     * Draws the islands accordingly to the given {@code GameState}
     *
     * @param gameState  the {@code GameState} to fetch the information about the islands from
     * @param pageWidth  the width of the screen
     * @param pageHeight the height of the screen
     * @param root       the {@code AnchorPane} to attach the islands to
     */
    private static void drawIslands(GameState gameState, double pageWidth, double pageHeight, AnchorPane root) {
        List<IslandState> islands = gameState.getIslands();
        double deltaAngle = (2 * Constants.PI) / islands.size();
        double radius = pageHeight * DrawingConstants.ISLAND_RADIUS;
        for (int i = 0; i < islands.size(); i++) {
            String path = "/gameboard/islands/Isola_" + ((i % 3) + 1) + ".png";
            ImageView island = getImageView(path, pageWidth * DrawingConstants.ISLAND_DIMENSION);
            Bounds imageBounds = island.boundsInParentProperty().get();
            double islandWidth = imageBounds.getWidth();
            double islandHeight = imageBounds.getHeight();
            BorderPane bp = new BorderPane(island);
            int steps = (i - gameState.getMNIndex() + gameState.getIslands().size()) % gameState.getIslands().size();
            int islandIndex = i;
            bp.setOnMouseClicked(event -> ObjectClickListeners.setIslandClicked(bp, steps, islandIndex));
            DrawingComponents.islands.add(bp);
            double X = Math.cos(deltaAngle * i) * radius;
            double Y = Math.sin(deltaAngle * i) * radius;
            double startingXIsland = pageWidth * DrawingConstants.ISLAND_X - islandWidth / 2 + X;
            double startingYIsland = pageHeight * DrawingConstants.ISLAND_Y - islandHeight / 2 + Y;
            bp.setLayoutX(startingXIsland);
            bp.setLayoutY(startingYIsland);
            root.getChildren().add(bp);

            VBox elementsOnIsland = new VBox();
            GridPane studentsOnIsland = new GridPane();
            HBox towersOnIsland = new HBox();
            HBox noEntryOnIsland = new HBox();
            elementsOnIsland.setLayoutX(pageWidth * DrawingConstants.ISLAND_ELEMENTS_X);
            elementsOnIsland.setLayoutY(pageHeight * DrawingConstants.ISLAND_ELEMENTS_Y);
            elementsOnIsland.getChildren().add(studentsOnIsland);
            elementsOnIsland.getChildren().add(towersOnIsland);
            elementsOnIsland.getChildren().add(noEntryOnIsland);

            for (int j = 0; j < islands.get(i).getNumOfTowers(); j++) {
                String towerPath = "/gameboard/towers/" + islands.get(i).getTowerColor().toString().toLowerCase() + "_tower.png";
                ImageView tower = getImageView(towerPath, islandWidth / DrawingConstants.ISLAND_TOWER_DIVISOR);
                towersOnIsland.getChildren().add(tower);
            }

            int lastRow = 1;
            List<BorderPane> studentsToDraw = new ArrayList<>();
            for (int j = 0; j < islands.get(i).getStudents().size(); j++) {
                Student s = islands.get(i).getStudents().get(j);
                String studentPath = "/gameboard/students/student_" + s.getColor().toString().toLowerCase() + ".png";
                ImageView student = getImageView(studentPath, islandWidth / DrawingConstants.ISLAND_STUDENT_DIVISOR);
                BorderPane studentBorderPane = new BorderPane(student);
                studentBorderPane.setOnMouseClicked(event ->
                        ObjectClickListeners.setStudentOnIslandClicked(studentBorderPane, s.getColor(), islandIndex));
                studentsToDraw.add(studentBorderPane);

                studentsOnIsland.add(studentBorderPane, j % 4, j / 4 + 1);
            }
            DrawingComponents.studentsOnIslands.put(i, studentsToDraw);

            for (int j = 0; j < islands.get(i).getNoEntryNum(); j++) {
                ImageView noEntry = getImageView("/gameboard/deny_island_icon.png", islandWidth / DrawingConstants.ISLAND_NOENTRY_DIVISOR);

                BorderPane noEntryBorderPane = new BorderPane(noEntry);
                noEntryBorderPane.setOnMouseClicked(event -> {
                }); // TODO remember to pass island index
                noEntriesOnIslands.add(noEntryBorderPane);
                noEntryOnIsland.getChildren().add(noEntryBorderPane);
            }

            bp.getChildren().add(elementsOnIsland);

            if (i == gameState.getMNIndex()) {
                ImageView mn = getImageView("/gameboard/mother_nature.png", islandWidth * DrawingConstants.ISLAND_MN_DIM);
                BorderPane motherNature = new BorderPane(mn);
                motherNature.setLayoutX(0);
                motherNature.setLayoutY(islandHeight / DrawingConstants.ISLAND_MN_Y_DIVISOR);
                bp.getChildren().add(motherNature);
            }
        }
    }

    /**
     * Draws the assistants accordingly to the given {@code GameState}
     *
     * @param gameState  the {@code GameState} to fetch the information about the assistants from
     * @param pageWidth  the width of the screen
     * @param pageHeight the height of the screen
     * @param root       the {@code AnchorPane} to attach the islands to
     * @param username   the username of the player who owns the assistants
     */
    private static void drawAssistants(GameState gameState, double pageWidth, double pageHeight, AnchorPane root, String username) {
        PlayerState player = gameState.getPlayers().stream().filter(p -> p.getName().equals(username)).findAny().orElse(null);
        if (player != null && player.getAssistants() != null) {
            double initialX = dashboardHeight / DrawingConstants.DASHBOARD_HEIGHT_OVER_WIDTH + pageWidth * DrawingConstants.OFFSET_OF_FIRST_ASSISTANT;
            double finalX = pageWidth * (1 - DrawingConstants.OFFSET_OF_FIRST_ASSISTANT);
            GridPane assistants = getAssistants(player.getAssistants(), pageWidth, pageHeight, initialX, finalX);
            root.getChildren().add(assistants);
        }
    }

    /**
     * Returns a {@code GridPane} containing the assistants to be drawn
     *
     * @param assistants the {@code Array} of the assistants to be drawn
     * @param pageWidth  the width of the screen
     * @param pageHeight the height of the screen
     * @param initialX   the initial X coordinate for the assistants
     * @param finalX     the final X coordinate for the assistants
     * @return a {@code GridPane} containing the assistants to be drawn
     */
    private static GridPane getAssistants(int[] assistants, double pageWidth, double pageHeight, double initialX, double finalX) {
        if (assistants.length == 0) return new GridPane();
        double initialY = pageHeight * DrawingConstants.ASSISTANT_Y;
        double spaceForAssistants = finalX - initialX - (assistants.length - 1) * pageWidth * DrawingConstants.OFFSET_BETWEEN_ASSISTANTS;
        GridPane gridPane = new GridPane();
        gridPane.setLayoutX(initialX);
        gridPane.setLayoutY(initialY);
        gridPane.setHgap(pageWidth * DrawingConstants.OFFSET_BETWEEN_ASSISTANTS);
        gridPane.setVgap(pageHeight * DrawingConstants.OFFSET_OF_FIRST_ASSISTANT);

        for (int value : assistants) {
            String path = "/gameboard/assistants/Assistente (" + value + ").png";
            double minWidth = DrawingConstants.ASSISTANT_MAX_WIDTH_SINGLE_LINE;
            ImageView assistant = getImageView(path, spaceForAssistants * Math.min((double) 2 / assistants.length, minWidth));

            BorderPane assistantPane = new BorderPane(assistant);
            assistantPane.setOnMouseClicked(event -> ObjectClickListeners.setAssistantClicked(value, assistantPane));
            assistantCards.add(assistantPane);

            gridPane.add(assistantPane, value - 1, 0);
        }

        return gridPane;
    }

    /**
     * Draws the given clouds with the correct students on each one
     *
     * @param clouds     the {@code List} of clouds to be drawn
     * @param pageWidth  the width of the screen
     * @param pageHeight the height of the screen
     * @param root       the {@code AnchorPane} to attach the clouds to
     */
    private static void drawClouds(List<CloudState> clouds, double pageWidth, double pageHeight, AnchorPane root) {
        GridPane cloudGrid = new GridPane();
        double layoutX = dashboardHeight / DrawingConstants.DASHBOARD_HEIGHT_OVER_WIDTH + pageWidth * DrawingConstants.OFFSET_OF_CLOUD_FROM_BORDER;
        cloudGrid.setLayoutY(pageHeight * DrawingConstants.CLOUD_STARTING_HEIGHT);
        cloudGrid.setLayoutX(layoutX);
        cloudGrid.setHgap(pageHeight * DrawingConstants.SPACE_BETWEEN_CLOUDS);
        cloudGrid.setVgap(pageHeight * DrawingConstants.SPACE_BETWEEN_CLOUDS);

        for (int i = 0; i < clouds.size(); i++) {
            CloudState cloud = clouds.get(i);
            AnchorPane cloudToDraw = getCloudWithStudents(cloud, pageWidth, pageHeight);
            int cloudIndex = i;
            cloudToDraw.setOnMouseClicked(event -> ObjectClickListeners.setCloudClicked(cloudToDraw, cloudIndex));
            cloudGrid.add(cloudToDraw, i % 2, i / 2);
            cloudImages.add(cloudToDraw);
        }
        root.getChildren().add(cloudGrid);
    }

    /**
     * Returns an {@code AnchorPane} containing the given cloud with the correct students on it
     *
     * @param cloud      the cloud to put te correct students on
     * @param pageWidth  the width of the screen
     * @param pageHeight the height of the screen
     * @return an {@code AnchorPane} containing the given cloud with the correct students on it
     */
    private static AnchorPane getCloudWithStudents(CloudState cloud, double pageWidth, double pageHeight) {
        ImageView cloudBackground = new ImageView(new Image("/gameboard/clouds/cloud_card.png"));
        cloudBackground.setPreserveRatio(true);
        cloudBackground.setFitHeight(pageHeight * DrawingConstants.CLOUD_HEIGHT);

        GridPane studentsPane = new GridPane();
        Bounds imageBounds = cloudBackground.boundsInParentProperty().get();
        studentsPane.setLayoutX(imageBounds.getWidth() * DrawingConstants.OFFSET_OF_STUDENT_FROM_CLOUD);
        studentsPane.setLayoutY(imageBounds.getHeight() * DrawingConstants.OFFSET_OF_STUDENT_FROM_CLOUD);
        studentsPane.setHgap(imageBounds.getWidth() * DrawingConstants.OFFSET_BETWEEN_STUDENTS_IN_CLOUD);
        studentsPane.setVgap(imageBounds.getHeight() * DrawingConstants.OFFSET_BETWEEN_STUDENTS_IN_CLOUD);

        if (cloud.getStudents() != null) {
            for (int j = 0; j < cloud.getStudents().size(); j++) {
                String studentPath = "/gameboard/students/student_" +
                        cloud.getStudents().get(j).getColor().toString().toLowerCase() + ".png";
                ImageView student = getImageView(studentPath, pageWidth * DrawingConstants.CLOUD_STUDENT_DIM);

                studentsPane.add(student, j % 2, j / 2);
            }
        }

        return new AnchorPane(cloudBackground, studentsPane);
    }

    /**
     * Draws the given characters with the correct students on each one (if they have some)
     *
     * @param characters the {@code List} of characters to be drawn
     * @param pageWidth  the width of the screen
     * @param pageHeight the height of the screen
     * @param root       the {@code AnchorPane} to attach the clouds to
     */
    private static void drawCharacters(List<CharacterState> characters, double pageWidth, double pageHeight, AnchorPane root) {
        double coordX = dashboardHeight / DrawingConstants.DASHBOARD_HEIGHT_OVER_WIDTH + pageWidth * DrawingConstants.CHARACTERS_BEGINNING_PROPORTION;
        int heightProportion = 13;

        for (CharacterState character : characters) {
            String imagePath = "/gameboard/characters/" + character.getCharacterName() + ".jpg";
            ImageView characterImage = getImageView(imagePath, pageWidth * DrawingConstants.CHARACTER_CARD_PROPORTION);

            BorderPane characterToAdd = new BorderPane(characterImage);
            characterToAdd.setLayoutX(coordX);
            characterToAdd.setLayoutY(pageHeight / heightProportion);
            characterToAdd.setOnMouseClicked(event -> ObjectClickListeners.setCharacterClicked(character.getCharacterName(), characterToAdd));
            characterImages.add(characterToAdd);
            root.getChildren().add(characterToAdd);

            if (character.isHasCoin()) {
                double imageWidth = pageWidth * DrawingConstants.COIN_PROPORTION;
                ImageView coin = getCoinImageView(
                        coordX + pageWidth * (DrawingConstants.CHARACTER_CARD_PROPORTION) - imageWidth,
                        pageHeight * DrawingConstants.CHARACTER_COIN_DIM,
                        imageWidth);
                root.getChildren().add(coin);
            }
            if (character.getStudents() != null) {
                GridPane grid = new GridPane();
                grid.setLayoutX(coordX + pageWidth * DrawingConstants.SPACE_BETWEEN_STUDENTS_ON_CHARACTERS);
                grid.setLayoutY(pageHeight / heightProportion - 2 * pageWidth * DrawingConstants.CHARACTER_STUDENT_DIM);
                List<BorderPane> studentOnCharacter = new ArrayList<>();
                for (int i = 0; i < character.getStudents().size(); i++) {
                    String studentPath = "/gameboard/students/student_" +
                            character.getStudents().get(i).getColor().toString().toLowerCase() + ".png";
                    ImageView student = getImageView(studentPath, pageWidth * DrawingConstants.CHARACTER_STUDENT_DIM);

                    BorderPane studentPane = new BorderPane(student);
                    int index = i;
                    studentPane.setOnMouseClicked(event ->
                            ObjectClickListeners.setStudentOnCharacterClicked(
                                    character.getStudents().get(index).getColor(), studentPane))
                    ;
                    studentOnCharacter.add(studentPane);

                    grid.add(studentPane, i / 2, i % 2);
                }
                studentsOnCharacter.put(character.getCharacterName(), studentOnCharacter);

                root.getChildren().add(grid);
            }

            coordX += pageWidth * (DrawingConstants.CHARACTER_CARD_PROPORTION + DrawingConstants.SPACE_BETWEEN_CHARACTERS_PROPORTION);
        }
    }

    /**
     * Returns an {@code ImageView} containing the {@code Image} from the given path
     *
     * @param x        the starting X coordinate of the {@code ImageView}
     * @param y        the starting Y coordinate of the {@code ImageView}
     * @param fitWidth the witdth to be set to the {@code ImageView}
     * @return an {@code ImageView} containing the {@code Image} from the given path
     */
    private static ImageView getCoinImageView(double x, double y, double fitWidth) {
        ImageView iv = new ImageView(new Image("/gameboard/Moneta_base.png"));
        iv.setPreserveRatio(true);
        iv.setFitWidth(fitWidth);
        iv.setX(x);
        iv.setY(y);
        return iv;
    }

    private static ImageView getImageView(String path, double fitWidth) {
        ImageView iv = new ImageView(new Image(path));
        iv.setPreserveRatio(true);
        iv.setFitWidth(fitWidth);
        return iv;
    }

    /**
     * Returns a {@code GridPane} accordingly to the given parameters
     *
     * @param x    the starting X coordinate of the {@code GridPane}
     * @param y    the starting Y coordinate of the {@code GridPane}
     * @param hgap the gap to be set horizontally between two elements of the {@code GridPane}
     * @param vgap the gap to be set vertically between two elements of the {@code GridPane}
     * @return a {@code GridPane} accordingly to the given parameters
     */
    private static GridPane getGridPane(double x, double y, double hgap, double vgap) {
        GridPane grid = new GridPane();
        grid.setLayoutX(x);
        grid.setLayoutY(y);
        grid.setHgap(hgap);
        grid.setVgap(vgap);
        return grid;
    }

    /**
     * Highlights with the correct color the given actions
     *
     * @param currentActions the list of actions to be highlighted
     */
    public static void highlightCurrentActions(List<String> currentActions) {
        lastActions = currentActions;
        for (String action : currentActions) {
            switch (action) {
                case Messages.ACTION_MOVE_STUDENT_TO_DINING, Messages.ACTION_MOVE_STUDENT_TO_ISLAND -> {
                    entranceStudents.forEach(DrawingComponents::setGoldenBorder);
                    diningGaps.forEach(DrawingComponents::setGoldenBorder);
                    islands.forEach(DrawingComponents::setGoldenBorder);
                }
                case Messages.ACTION_PLAY_ASSISTANT -> assistantCards.forEach(DrawingComponents::setGoldenBorder);
                case Messages.ACTION_PLAY_CHARACTER -> characterImages.forEach(DrawingComponents::setGoldenBorder);
                case Messages.ACTION_MOVE_MN -> islands.forEach(DrawingComponents::setGoldenBorder);
                case Messages.ACTION_FILL_FROM_CLOUD -> cloudImages.forEach(DrawingComponents::setGoldenBorder);
            }
        }
    }

    /**
     * Removes the golden border from all {@code Characters}
     */
    public static void removeGoldenBordersFromAllCharacters() {
        characterImages.forEach(character -> character.getStyleClass().clear());
    }

    /**
     * Sets a golden border to the given element
     *
     * @param element the element to set the golden border to
     */
    private static void setGoldenBorder(Node element) {
        element.getStyleClass().add(DrawingConstants.STYLE_HIGHLIGHT);
    }

    /**
     * Sets a golden blue to the given element
     *
     * @param element the element to set the golden border to
     */
    private static void setBlueBorders(Node element) {
        element.getStyleClass().add(DrawingConstants.STYLE_SWAP_CHARACTER_A);
    }

    /**
     * Sets a green border to the given element
     *
     * @param element the element to set the golden border to
     */
    private static void setGreenBorders(Node element) {
        element.getStyleClass().add(DrawingConstants.STYLE_MOVING_CHARACTER_A);
    }

    /**
     * Sets a blue border to the students in the {@code Entrance}
     */
    public static void setBlueBordersToEntranceStudents() {
        entranceStudents.forEach(DrawingComponents::setBlueBorders);
    }

    /**
     * Sets a blue border on the students on the given {@code Character}
     *
     * @param name the name of the {@code Character} whose students to set a blue border to
     */
    public static void setBlueBordersToCharacterStudents(CharacterName name) {
        studentsOnCharacter.get(name).forEach(DrawingComponents::setBlueBorders);
    }

    /**
     * Sets a blue border to the students in the {@code DinigRoom}
     */
    public static void setBlueBordersToDiningStudents() {
        diningStudents.forEach(DrawingComponents::setBlueBorders);
    }

    /**
     * Moves the selected {@code Student} away from the {@code Character} card to an {@code Island} or to the {@code DiningRoom}
     *
     * @param name     the name of the {@code Character} to move the selected {@code Student} from
     * @param toIsland true if the selected {@code Student} should be moved to an {@code Island}, false if it should be moved to the {@code DiningRoom}
     */
    public static void moveStudentAwayFromCard(CharacterName name, boolean toIsland) {
        if (studentsOnCharacter.containsKey(name)) {
            removeGoldenBordersFromAllElements();
            List<BorderPane> students = studentsOnCharacter.get(name);
            students.forEach(DrawingComponents::setGreenBorders);
            if (toIsland) {
                islands.forEach(DrawingComponents::setGreenBorders);
            } else {
                diningGaps.forEach(DrawingComponents::setGreenBorders);
            }
        }
    }

    /**
     * Makes every island able to be chosen by the user to fulfill an effect of a {@code Character} which involves an {@code Island}
     */
    public static void askIslandIndex() {
        removeGoldenBordersFromAllElements();
        islands.forEach(island -> island.getStyleClass().add(DrawingConstants.STYLE_ISLAND_CHARACTER_A));
    }

    /**
     * Removes the golden border from all the elements in the gameboard
     */
    public static void removeGoldenBordersFromAllElements() {
        diningGaps.forEach(gap -> gap.getStyleClass().clear());
        entranceStudents.forEach(student -> student.getStyleClass().clear());
        assistantCards.forEach(assistant -> assistant.getStyleClass().clear());
        cloudImages.forEach(cloud -> cloud.getStyleClass().clear());
        assistantCards.forEach(card -> card.getStyleClass().clear());
        islands.forEach(island -> island.getStyleClass().clear());
        studentsOnCharacter.values().forEach(list -> list.forEach(student -> student.getStyleClass().clear()));
        characterImages.forEach(character -> character.getStyleClass().clear());
    }

    /**
     * Clears every static list of elements which could be highlighted and also clears the given {@code AnchorPane}
     *
     * @param root the {@code AnchorPane} to be cleared
     */
    public static void clearAll(AnchorPane root) {
        root.getChildren().clear();

        characterImages.clear();
        cloudImages.clear();
        assistantCards.clear();
        entranceStudents.clear();
        diningStudents.clear();
        diningGaps.clear();
        studentsOnCharacter.clear();
        studentsOnIslands.clear();
        noEntriesOnIslands.clear();
        islands.clear();
    }

    public static List<String> getLastActions() {
        return lastActions;
    }
}
