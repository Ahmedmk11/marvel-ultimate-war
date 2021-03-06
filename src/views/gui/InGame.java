package views.gui;

import engine.Game;
import engine.Player;
import engine.PriorityQueue;
import exceptions.*;
import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import model.abilities.*;
import model.effects.Effect;
import model.world.*;

import java.util.ArrayList;

import static views.gui.GameApp.popUp;

public class InGame {
    static Player player1;
    static Player player2;
    static Game newGame;
    static boolean singleTarget = false;
    static boolean abt1 = false;
    static boolean abt2 = false;
    static boolean abt3 = false;
    static HBox menu = new HBox();
    static HBox profiles = new HBox();
    static GridPane board = new GridPane();
    static VBox right = new VBox();
    static HBox HUD = new HBox();

    public static Scene create(Game newGame) {
        InGame.newGame = newGame;
        player1 = newGame.getFirstPlayer();
        player2 = newGame.getSecondPlayer();

        Button quit = new Button("Quit");
        quit.setPrefSize(100, 50);
        quit.setOnAction(e -> GameApp.onQuit());
        quit.styleProperty().bind(Bindings.when(quit.hoverProperty()).then("-fx-cursor: hand; -fx-scale-x: 1.1;" +
                        " -fx-scale-y: 1.1;-fx-background-color: #090a0c, linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%)," +
                        " linear-gradient(#20262b, #191d22), radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9)," +
                        " rgba(255,255,255,0));-fx-background-radius: 5,4,3,5; -fx-background-insets: 0,1,2,0;" +
                        "-fx-effect: dropshadow(three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1);-fx-text-fill: white;")
                .otherwise("-fx-background-color: #090a0c, linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%)," + "  linear-gradient(#20262b, #191d22)" +
                        ", radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9), rgba(255,255,255,0));-fx-text-fill: white;"));
        quit.setFont(Font.font("Arial", 24));
        menu.getChildren().add(quit);
        menu.getChildren().add(createTurnOrder());
        menu.setMaxHeight(160);
        menu.setPrefWidth(1600);
        menu.setPadding(new Insets(0, 0, 10, 0));
        menu.setSpacing(20);
        menu.setAlignment(Pos.CENTER_LEFT);

        profiles.getChildren().add(createProfile(player1, 1));
        profiles.getChildren().add(createProfile(player2, 2));
        profiles.addEventFilter(KeyEvent.ANY, Event::consume);
        profiles.setPrefWidth(450);
        profiles.setPadding(new Insets(0, 10, 0, 0));

        for (int i = 0; i < 5; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / 5);
            colConst.setHgrow(Priority.ALWAYS);
            colConst.setFillWidth(true);
            board.getColumnConstraints().add(colConst);
        }
        for (int i = 0; i < 5; i++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / 5);
            rowConst.setVgrow(Priority.ALWAYS);
            rowConst.setFillHeight(true);
            board.getRowConstraints().add(rowConst);
        }
        board.getChildren().addAll(createBoard());

        right.getChildren().add(champAbilities(newGame.getCurrentChampion()));
        right.setPrefWidth(250);
        right.setPadding(new Insets(0, 10, 0, 0));

        ToggleButton attack = new ToggleButton("Attack");
        attack.setPrefSize(100, 50);
        HUD.getChildren().add(attack);
        Button leaderAbility = new Button("Use Leader Ability");
        leaderAbility.setPrefHeight(50);
        leaderAbility.setOnAction(e -> {
            try {
                newGame.useLeaderAbility();
            } catch (LeaderNotCurrentException | LeaderAbilityAlreadyUsedException ex) {
                popUp(ex);
            }
            update();
        });
        HUD.getChildren().add(leaderAbility);
        ToggleButton cast1 = new ToggleButton("Ability 1");
        cast1.setOnAction(e -> {
            if (newGame.getCurrentChampion().getAbilities().get(0).getCastArea() == AreaOfEffect.SINGLETARGET) {
                singleTarget = true;
                abt1 = true;
                cast1.selectedProperty().setValue(false);
            }
            if (newGame.getCurrentChampion().getAbilities().get(0).getCastArea() != AreaOfEffect.DIRECTIONAL) {
                try {
                    handleAbility(0);
                } catch (AbilityUseException | NotEnoughResourcesException | CloneNotSupportedException ex) {
                    popUp(ex);
                }
                cast1.selectedProperty().setValue(false);
                update();
            }
        });
        cast1.setPrefSize(100, 50);
        HUD.getChildren().add(cast1);
        ToggleButton cast2 = new ToggleButton("Ability 2");
        cast2.setOnAction(e -> {
            if (newGame.getCurrentChampion().getAbilities().get(1).getCastArea() == AreaOfEffect.SINGLETARGET) {
                singleTarget = true;
                abt2 = true;
                cast1.selectedProperty().setValue(false);
            }
            if (newGame.getCurrentChampion().getAbilities().get(1).getCastArea() != AreaOfEffect.DIRECTIONAL) {
                try {
                    handleAbility(1);
                } catch (AbilityUseException | NotEnoughResourcesException | CloneNotSupportedException ex) {
                    popUp(ex);
                }
                cast2.selectedProperty().setValue(false);
                update();
            }
        });
        cast2.setPrefSize(100, 50);
        HUD.getChildren().add(cast2);
        ToggleButton cast3 = new ToggleButton("Ability 3");
        cast3.setOnAction(e -> {
            if (newGame.getCurrentChampion().getAbilities().get(2).getCastArea() == AreaOfEffect.SINGLETARGET) {
                singleTarget = true;
                abt3 = true;
                cast1.selectedProperty().setValue(false);
            }
            if (newGame.getCurrentChampion().getAbilities().get(2).getCastArea() != AreaOfEffect.DIRECTIONAL) {
                try {
                    handleAbility(2);
                } catch (AbilityUseException | NotEnoughResourcesException | CloneNotSupportedException ex) {
                    popUp(ex);
                }
                cast3.selectedProperty().setValue(false);
                update();
            }
        });
        cast3.setPrefSize(100, 50);
        HUD.getChildren().add(cast3);
        Button endTurn = new Button("End Turn");
        endTurn.setOnAction(e -> {
            newGame.endTurn();
            right.getChildren().clear();
            right.getChildren().add(champAbilities(newGame.getCurrentChampion()));
            menu.getChildren().remove(menu.getChildren().size() - 1);
            menu.getChildren().add(createTurnOrder());
            board.getChildren().clear();
            board.getChildren().addAll(createBoard());
            profiles.getChildren().clear();
            profiles.getChildren().add(createProfile(player1, 1));
            profiles.getChildren().add(createProfile(player2, 2));
        });
        endTurn.setPrefSize(100, 50);
        HUD.getChildren().add(endTurn);
        HUD.setMaxHeight(150);
        HUD.setSpacing(10);
        HUD.setAlignment(Pos.CENTER);

        Pane boardPane = new Pane();
        boardPane.setPadding(new Insets(10, 10, 5, 10));
        boardPane.getChildren().add(board);

        BorderPane root = new BorderPane(boardPane, menu, right, HUD, profiles);
        root.addEventFilter(KeyEvent.KEY_RELEASED, key -> {
            if (key.getCode() == KeyCode.TAB)
                return;
            if (attack.isSelected()) {
                attack.selectedProperty().setValue(false);
                handleAttack(key);
            } else if (cast1.isSelected()) {
                if (newGame.getCurrentChampion().getAbilities().get(0).getCastArea() == AreaOfEffect.DIRECTIONAL)
                    handleDirectional(key, newGame.getCurrentChampion().getAbilities().get(0));
                cast1.selectedProperty().setValue(false);
            } else if (cast2.isSelected()) {
                if (newGame.getCurrentChampion().getAbilities().get(1).getCastArea() == AreaOfEffect.DIRECTIONAL)
                    handleDirectional(key, newGame.getCurrentChampion().getAbilities().get(1));
                cast2.selectedProperty().setValue(false);
            } else if (cast3.isSelected()) {
                if (newGame.getCurrentChampion().getAbilities().get(2).getCastArea() == AreaOfEffect.DIRECTIONAL)
                    handleDirectional(key, newGame.getCurrentChampion().getAbilities().get(2));
                cast1.selectedProperty().setValue(false);
            } else
                handleMove(key);
            update();
        });

        attack.styleProperty().bind(Bindings.when(attack.hoverProperty()).then("-fx-cursor: hand; -fx-scale-x: 1.1;" +
                        " -fx-scale-y: 1.1;-fx-background-color: #090a0c, linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%)," +
                        " linear-gradient(#20262b, #191d22), radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9)," +
                        " rgba(255,255,255,0));-fx-background-radius: 5,4,3,5; -fx-background-insets: 0,1,2,0;" +
                        "-fx-effect: dropshadow(three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1);-fx-text-fill: white;")
                .otherwise("-fx-background-color: #090a0c, linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%)," + "  linear-gradient(#20262b, #191d22)" +
                        ", radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9), rgba(255,255,255,0));-fx-text-fill: white;"));
        attack.setFont(Font.font("Arial", 16));

        leaderAbility.styleProperty().bind(Bindings.when(leaderAbility.hoverProperty()).then("-fx-cursor: hand; -fx-scale-x: 1.1;" +
                        " -fx-scale-y: 1.1;-fx-background-color: #090a0c, linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%)," +
                        " linear-gradient(#20262b, #191d22), radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9)," +
                        " rgba(255,255,255,0));-fx-background-radius: 5,4,3,5; -fx-background-insets: 0,1,2,0;" +
                        "-fx-effect: dropshadow(three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1);-fx-text-fill: white;")
                .otherwise("-fx-background-color: #090a0c, linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%)," + "  linear-gradient(#20262b, #191d22)" +
                        ", radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9), rgba(255,255,255,0));-fx-text-fill: white;"));
        leaderAbility.setFont(Font.font("Arial", 16));


        cast1.styleProperty().bind(Bindings.when(cast1.hoverProperty()).then("-fx-cursor: hand; -fx-scale-x: 1.1;" +
                        " -fx-scale-y: 1.1;-fx-background-color: #090a0c, linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%)," +
                        " linear-gradient(#20262b, #191d22), radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9)," +
                        " rgba(255,255,255,0));-fx-background-radius: 5,4,3,5; -fx-background-insets: 0,1,2,0;" +
                        "-fx-effect: dropshadow(three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1);-fx-text-fill: white;")
                .otherwise("-fx-background-color: #090a0c, linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%)," + "  linear-gradient(#20262b, #191d22)" +
                        ", radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9), rgba(255,255,255,0));-fx-text-fill: white;"));
        cast1.setFont(Font.font("Arial", 16));


        cast2.styleProperty().bind(Bindings.when(cast2.hoverProperty()).then("-fx-cursor: hand; -fx-scale-x: 1.1;" +
                        " -fx-scale-y: 1.1;-fx-background-color: #090a0c, linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%)," +
                        " linear-gradient(#20262b, #191d22), radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9)," +
                        " rgba(255,255,255,0));-fx-background-radius: 5,4,3,5; -fx-background-insets: 0,1,2,0;" +
                        "-fx-effect: dropshadow(three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1);-fx-text-fill: white;")
                .otherwise("-fx-background-color: #090a0c, linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%)," + "  linear-gradient(#20262b, #191d22)" +
                        ", radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9), rgba(255,255,255,0));-fx-text-fill: white;"));
        cast2.setFont(Font.font("Arial", 16));


        cast3.styleProperty().bind(Bindings.when(cast3.hoverProperty()).then("-fx-cursor: hand; -fx-scale-x: 1.1;" +
                        " -fx-scale-y: 1.1;-fx-background-color: #090a0c, linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%)," +
                        " linear-gradient(#20262b, #191d22), radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9)," +
                        " rgba(255,255,255,0));-fx-background-radius: 5,4,3,5; -fx-background-insets: 0,1,2,0;" +
                        "-fx-effect: dropshadow(three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1);-fx-text-fill: white;")
                .otherwise("-fx-background-color: #090a0c, linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%)," + "  linear-gradient(#20262b, #191d22)" +
                        ", radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9), rgba(255,255,255,0));-fx-text-fill: white;"));
        cast3.setFont(Font.font("Arial", 16));


        endTurn.styleProperty().bind(Bindings.when(endTurn.hoverProperty()).then("-fx-cursor: hand; -fx-scale-x: 1.1;" +
                        " -fx-scale-y: 1.1;-fx-background-color: #090a0c, linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%)," +
                        " linear-gradient(#20262b, #191d22), radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9)," +
                        " rgba(255,255,255,0));-fx-background-radius: 5,4,3,5; -fx-background-insets: 0,1,2,0;" +
                        "-fx-effect: dropshadow(three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1);-fx-text-fill: white;")
                .otherwise("-fx-background-color: #090a0c, linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%)," + "  linear-gradient(#20262b, #191d22)" +
                        ", radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9), rgba(255,255,255,0));-fx-text-fill: white;"));
        endTurn.setFont(Font.font("Arial", 16));


        root.setPadding(new Insets(20, 20, 20, 20));
        root.setStyle("-fx-font-size: 16; -fx-font-weight: 700");


        return new Scene(root, 1600, 900);
    }

    private static VBox champInfo(Champion c) {
        VBox currentChampInfo = new VBox();
        ImageView champView = new ImageView(new Image("views/assets/champions/%s.png".formatted(c.getName())));
        champView.setFitWidth(40);
        champView.setFitHeight(40);
        VBox nameBox = new VBox();
        String title = (c == player1.getLeader() || c == player2.getLeader()) ? "(L) " : "";
        title += (c == newGame.getCurrentChampion()) ? "(C) " : "";
        title += c.getName();
        nameBox.getChildren().add(new Label(title));
        if (c instanceof Hero) {
            nameBox.getChildren().add(new Label("Hero"));
        } else if (c instanceof Villain) {
            nameBox.getChildren().add(new Label("Villain"));
        } else if (c instanceof AntiHero) {
            nameBox.getChildren().add(new Label("AntiHero"));
        }
        nameBox.getChildren().add(new Label("HP: " + c.getCurrentHP() + "/" + c.getMaxHP()));
        nameBox.setAlignment(Pos.CENTER);
        HBox topBox = new HBox();
        topBox.getChildren().add(champView);
        topBox.getChildren().add(nameBox);
        currentChampInfo.getChildren().add(topBox);
        topBox.setSpacing(10);
        GridPane stats = new GridPane();
        stats.add(new Label("Mana: " + c.getMana()), 0, 0, 1, 1);
        stats.add(new Label("Actions: " + c.getCurrentActionPoints() + "/" + c.getMaxActionPointsPerTurn()), 1, 0, 1, 1);
        stats.add(new Label("Damage: " + c.getAttackDamage()), 0, 1, 1, 1);
        stats.add(new Label("Range: " + c.getAttackRange()), 1, 1, 1, 1);
        stats.setVgap(5);
        stats.setHgap(5);
        currentChampInfo.getChildren().add(stats);
        TilePane effects = new TilePane(Orientation.HORIZONTAL);
        effects.getChildren().add(new Label("Effects: "));
        for (Effect eft : c.getAppliedEffects())
            effects.getChildren().add(new Label(eft.getName() + " (" + eft.getDuration() + ") "));
        currentChampInfo.getChildren().add(effects);
        currentChampInfo.setAlignment(Pos.CENTER_LEFT);
        return currentChampInfo;
    }

    private static VBox champAbilities(Champion c) {
        VBox currentChampAbilities = new VBox();
        for (Ability abt : c.getAbilities()) {
            VBox abilityInfo = new VBox();
            String name = "" + abt.getName();
            String value = "";
            if (abt instanceof DamagingAbility) {
                name += " (Damaging)";
                value += "Damage Amount: " + ((DamagingAbility) abt).getDamageAmount();
            } else if (abt instanceof HealingAbility) {
                name += " (Healing)";
                value += "Heal Amount: " + ((HealingAbility) abt).getHealAmount();
            } else if (abt instanceof CrowdControlAbility) {
                name += " (CC)";
                value += "Effect: " + ((CrowdControlAbility) abt).getEffect().getName();
            }
            abilityInfo.getChildren().add(new Label(name));
            abilityInfo.getChildren().add(new Label(value));
            abilityInfo.getChildren().add(new Label("AoE: " + abt.getCastArea()));
            abilityInfo.getChildren().add(new Label("Range: " + abt.getCastRange()));
            abilityInfo.getChildren().add(new Label("Mana Cost: " + abt.getManaCost()));
            abilityInfo.getChildren().add(new Label("AP Cost: " + abt.getRequiredActionPoints()));
            abilityInfo.getChildren().add(new Label("Cooldown: " + abt.getCurrentCooldown() + "/" + abt.getBaseCooldown()));
            abilityInfo.setAlignment(Pos.TOP_RIGHT);
            currentChampAbilities.getChildren().add(abilityInfo);
        }
        currentChampAbilities.setSpacing(15);
        currentChampAbilities.setAlignment(Pos.TOP_RIGHT);
        return currentChampAbilities;
    }

    private static VBox createProfile(Player player, int i) {
        VBox profile = new VBox();
        Label name = new Label(player.getName());
        name.setStyle("-fx-font-size: 20px");
        profile.getChildren().add(name);
        if (i == 1 && newGame.isFirstLeaderAbilityUsed())
            profile.getChildren().add(new Label("Leader Ability Used"));
        else if (i == 2 && newGame.isSecondLeaderAbilityUsed())
            profile.getChildren().add(new Label("Leader Ability Used"));
        else
            profile.getChildren().add(new Label("Leader Ability Not Used"));
        VBox team = new VBox();
        for (Champion c : player.getTeam()) {
            team.getChildren().add(champInfo(c));
        }
        team.setSpacing(5);
        team.setPadding(new Insets(0, 0, 5, 0));
        profile.getChildren().add(team);
        profile.setSpacing(10);
        return profile;
    }

    private static ArrayList<Node> createBoard() {
        ArrayList<Node> boardTiles = new ArrayList<>();
        for (int i = 0; i < newGame.getBoard().length; i++) {
            for (int j = 0; j < newGame.getBoard()[i].length; j++) {
                Object tile = newGame.getBoard()[i][j];
                if (tile != null) {
                    if (tile instanceof Champion ch) {
                        ImageView iv = new ImageView(new Image("views/assets/champions/%s.png".formatted(ch.getName())));
                        Button btn = new Button();
                        btn.setGraphic(iv);
                        btn.setFocusTraversable(false);
                        btn.styleProperty().bind(Bindings.when(btn.hoverProperty()).then("-fx-cursor: hand;")
                                .otherwise("-fx-cursor: default;"));
                        GridPane.setConstraints(btn, ch.getLocation().y, 4 - ch.getLocation().x);
                        GridPane.setHalignment(btn, HPos.CENTER);
                        btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        btn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                            if (singleTarget) {
                                if (abt1) {
                                    handleSingleTarget(ch.getLocation().x, ch.getLocation().y, newGame.getCurrentChampion().getAbilities().get(0));
                                    abt1 = false;
                                } else if (abt2) {
                                    handleSingleTarget(ch.getLocation().x, ch.getLocation().y, newGame.getCurrentChampion().getAbilities().get(1));
                                    abt2 = false;
                                } else if (abt3) {
                                    handleSingleTarget(ch.getLocation().x, ch.getLocation().y, newGame.getCurrentChampion().getAbilities().get(2));
                                    abt3 = false;
                                }
                                singleTarget = false;
                            }
                        });
                        boardTiles.add(btn);
                    } else if (tile instanceof Cover cv) {
                        ImageView iv = new ImageView(new Image("views/assets/champions/wall.png"));
                        Button btn = new Button();
                        btn.setGraphic(iv);
                        Tooltip tt = new Tooltip("HP: " + cv.getCurrentHP());
                        tt.setStyle("-fx-font: normal bold 12 Langdon; "
                                + "-fx-base: #AE3522; "
                                + "-fx-text-fill: orange;");
                        btn.setTooltip(tt);
                        btn.setFocusTraversable(false);
                        btn.styleProperty().bind(Bindings.when(btn.hoverProperty()).then("-fx-cursor: hand;")
                                .otherwise("-fx-cursor: default;"));
                        GridPane.setConstraints(btn, cv.getLocation().y, 4 - cv.getLocation().x);
                        GridPane.setHalignment(btn, HPos.CENTER);
                        btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        boardTiles.add(btn);
                    }
                } else {
                    Button btn = new Button();
                    btn.setFocusTraversable(false);
                    btn.styleProperty().bind(Bindings.when(btn.hoverProperty()).then("-fx-cursor: hand;")
                            .otherwise("-fx-cursor: default;"));
                    GridPane.setConstraints(btn, j, 4 - i);
                    btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    boardTiles.add(btn);
                }
            }
        }
        return boardTiles;
    }

    public static TilePane createTurnOrder() {
        ArrayList<ImageView> turnImages = new ArrayList<>();
        int size = newGame.getTurnOrder().size();
        PriorityQueue pq = new PriorityQueue(size);
        for (int i = 0; i < size; i++) {
            Champion ch = (Champion) (newGame.getTurnOrder().remove());
            Image img = new Image("views/assets/champions/%s.png".formatted(ch.getName()));
            pq.insert(ch);
            ImageView iv = new ImageView(img);
            iv.setFitHeight(40);
            iv.setFitWidth(40);
            turnImages.add(iv);
        }
        for (int i = 0; i < size; i++)
            newGame.getTurnOrder().insert(pq.remove());
        TilePane turn = new TilePane(Orientation.HORIZONTAL);
        for (ImageView iv : turnImages) {
            turn.getChildren().add(iv);
            turn.getChildren().add(new Label(">"));
        }
        turn.getChildren().remove(turn.getChildren().size() - 1);
        turn.setPrefTileWidth(40);
        turn.setMaxHeight(40);
        turn.setPrefWidth(12 * 40);
        turn.setPadding(new Insets(0, 0, 0, 20));
        return turn;
    }

    private static void handleMove(KeyEvent key) {
        switch (key.getCode()) {
            case UP:
                try {
                    newGame.move(Direction.UP);
                } catch (UnallowedMovementException | NotEnoughResourcesException e) {
                    popUp(e);
                }
                break;
            case DOWN:
                try {
                    newGame.move(Direction.DOWN);
                } catch (UnallowedMovementException | NotEnoughResourcesException e) {
                    popUp(e);
                }
                break;
            case LEFT:
                try {
                    newGame.move(Direction.LEFT);
                } catch (UnallowedMovementException | NotEnoughResourcesException e) {
                    popUp(e);
                }
                break;
            case RIGHT:
                try {
                    newGame.move(Direction.RIGHT);
                } catch (UnallowedMovementException | NotEnoughResourcesException e) {
                    popUp(e);
                }
                break;
        }
    }

    private static void handleAttack(KeyEvent key) {
        switch (key.getCode()) {
            case UP:
                try {
                    newGame.attack(Direction.UP);
                } catch (InvalidTargetException | ChampionDisarmedException | NotEnoughResourcesException e) {
                    popUp(e);
                }
                break;
            case DOWN:
                try {
                    newGame.attack(Direction.DOWN);
                } catch (InvalidTargetException | ChampionDisarmedException | NotEnoughResourcesException e) {
                    popUp(e);
                }
                break;
            case LEFT:
                try {
                    newGame.attack(Direction.LEFT);
                } catch (InvalidTargetException | ChampionDisarmedException | NotEnoughResourcesException e) {
                    popUp(e);
                }
                break;
            case RIGHT:
                try {
                    newGame.attack(Direction.RIGHT);
                } catch (InvalidTargetException | ChampionDisarmedException | NotEnoughResourcesException e) {
                    popUp(e);
                }
                break;
        }
    }

    private static void handleDirectional(KeyEvent key, Ability abt) {
        switch (key.getCode()) {
            case UP:
                try {
                    newGame.castAbility(abt, Direction.UP);
                } catch (NotEnoughResourcesException | AbilityUseException | CloneNotSupportedException e) {
                    popUp(e);
                }
                break;
            case DOWN:
                try {
                    newGame.castAbility(abt, Direction.DOWN);
                } catch (NotEnoughResourcesException | AbilityUseException | CloneNotSupportedException e) {
                    popUp(e);
                }
                break;
            case LEFT:
                try {
                    newGame.castAbility(abt, Direction.LEFT);
                } catch (NotEnoughResourcesException | AbilityUseException | CloneNotSupportedException e) {
                    popUp(e);
                }
                break;
            case RIGHT:
                try {
                    newGame.castAbility(abt, Direction.RIGHT);
                } catch (NotEnoughResourcesException | AbilityUseException | CloneNotSupportedException e) {
                    popUp(e);
                }
                break;
        }
    }

    private static void handleSingleTarget(int x, int y, Ability ability) {
        try {
            newGame.castAbility(ability, x, y);
            update();
        } catch (AbilityUseException | InvalidTargetException | NotEnoughResourcesException |
                 CloneNotSupportedException e) {
            popUp(e);
        }
    }

    public static void handleAbility(int i) throws AbilityUseException, NotEnoughResourcesException, CloneNotSupportedException {
        ArrayList<Ability> abs = newGame.getCurrentChampion().getAbilities();
        if (abs.get(i).getCastArea() == AreaOfEffect.SELFTARGET)
            newGame.castAbility(abs.get(i));
        else if (abs.get(i).getCastArea() == AreaOfEffect.TEAMTARGET)
            newGame.castAbility(abs.get(i));
        else if (abs.get(i).getCastArea() == AreaOfEffect.SURROUND)
            newGame.castAbility(abs.get(i));
    }

    public static void update() {
        if (newGame.checkGameOver() != null)
            GameApp.onGameOver(newGame.checkGameOver());
        if (newGame.getCurrentChampion().getCurrentActionPoints() == 0) {
            newGame.endTurn();
            right.getChildren().clear();
            right.getChildren().add(champAbilities(newGame.getCurrentChampion()));
            menu.getChildren().remove(menu.getChildren().size() - 1);
            menu.getChildren().add(createTurnOrder());
        }
        board.getChildren().clear();
        board.getChildren().addAll(createBoard());
        profiles.getChildren().clear();
        profiles.getChildren().add(createProfile(player1, 1));
        profiles.getChildren().add(createProfile(player2, 2));
    }
}
