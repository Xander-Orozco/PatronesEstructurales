import java.util.Random;
import java.util.Scanner;

// =============================================================
// CLASE BASE: Personaje
// Representa a cualquier luchador del juego.
// =============================================================
class Personaje {
    private String nombre;
    private int puntosDeVida;
    private static final int MAX_DANO = 30;
    private static final int MIN_DANO = 10;

    public Personaje(String nombre) {
        this.nombre = nombre;
        this.puntosDeVida = 100;
    }

    public void atacar(Personaje oponente) {
        Random rand = new Random();
        int dano = rand.nextInt((MAX_DANO - MIN_DANO) + 1) + MIN_DANO;
        oponente.recibirDano(dano);
        System.out.println(this.nombre + " ataca a " + oponente.getNombre()
                + " causando " + dano + " puntos de daño.");
    }

    public void recibirDano(int dano) {
        this.puntosDeVida -= dano;
        if (this.puntosDeVida < 0) this.puntosDeVida = 0;
    }

    public boolean estaVivo() { return this.puntosDeVida > 0; }
    public String getNombre() { return this.nombre; }
    public int getPuntosDeVida() { return this.puntosDeVida; }
}

// =============================================================
// SUBCLASES (herencias requeridas para aplicar patrones)
// =============================================================

/**
 * Guerrero: luchador cuerpo a cuerpo con habilidad especial de golpe fuerte.
 * Incrementa su daño en un 20% adicional cuando usa golpeFuerte().
 */
class Guerrero extends Personaje {
    public Guerrero(String nombre) { super(nombre); }

    /** Ataque especial: inflige un daño base más un bono fijo de 15 puntos. */
    public void golpeFuerte(Personaje oponente) {
        int bonoDano = 15;
        oponente.recibirDano(bonoDano);
        System.out.println(getNombre() + " usa GOLPE FUERTE sobre " +
                oponente.getNombre() + " causando " + bonoDano + " puntos de bono!");
        atacar(oponente); // ataque normal adicional
    }
}

/**
 * Mago: luchador con magia que puede lanzar un hechizo cada 3 turnos.
 * Su hechizo ignora la defensa normal del oponente.
 */
class Mago extends Personaje {
    private int turnosDesdeHechizo = 0;
    private static final int COOLDOWN_HECHIZO = 3;

    public Mago(String nombre) { super(nombre); }

    /** Ataque mágico disponible cada 3 turnos: daño fijo de 40 puntos. */
    public void lanzarHechizo(Personaje oponente) {
        turnosDesdeHechizo++;
        if (turnosDesdeHechizo >= COOLDOWN_HECHIZO) {
            int danoMagico = 40;
            oponente.recibirDano(danoMagico);
            System.out.println(getNombre() + " lanza HECHIZO ARCANO sobre " +
                    oponente.getNombre() + " causando " + danoMagico + " de daño mágico!");
            turnosDesdeHechizo = 0;
        } else {
            System.out.println(getNombre() + " aún no puede usar el hechizo (" +
                    (COOLDOWN_HECHIZO - turnosDesdeHechizo) + " turno(s) restantes).");
            atacar(oponente);
        }
    }
}

// =============================================================
// SUBSISTEMA 1: UIManager
// Responsable de toda la entrada/salida con el usuario.
// =============================================================
class UIManager {
    private Scanner scanner = new Scanner(System.in);

    /** Solicita los nombres de ambos jugadores por consola. */
    public String[] pedirNombres() {
        System.out.print("Introduce el nombre del jugador 1: ");
        String nombre1 = scanner.nextLine();
        System.out.print("Introduce el nombre del jugador 2: ");
        String nombre2 = scanner.nextLine();
        return new String[]{nombre1, nombre2};
    }

    /** Muestra el estado de vida de un personaje. */
    public void mostrarEstado(Personaje p) {
        System.out.println("  >> " + p.getNombre() + " tiene " +
                p.getPuntosDeVida() + " HP restantes.");
    }

    /** Muestra un separador visual de turno. */
    public void mostrarTurno(String nombreAtacante) {
        System.out.println("\n--- Turno de: " + nombreAtacante + " ---");
    }

    /** Muestra el inicio de la pelea. */
    public void mostrarInicio(String n1, String n2) {
        System.out.println("\n===========================");
        System.out.println("  ¡La pelea comienza!");
        System.out.println("  " + n1 + " vs " + n2);
        System.out.println("===========================\n");
    }

    /** Muestra el resultado final de la pelea. */
    public void mostrarGanador(Personaje ganador) {
        System.out.println("\n===========================");
        System.out.println("  *** " + ganador.getNombre() + " GANA LA PELEA! ***");
        System.out.println("===========================\n");
    }
}

// =============================================================
// SUBSISTEMA 2: TurnManager
// Gestiona la lógica de turnos y condición de victoria.
// =============================================================
class TurnManager {
    private UIManager ui;

    public TurnManager(UIManager ui) { this.ui = ui; }

    /**
     * Ejecuta un turno: el atacante golpea al defensor.
     * Si el atacante es Mago, usa lanzarHechizo(); si es Guerrero, usa golpeFuerte().
     */
    public void ejecutarTurno(Personaje atacante, Personaje defensor) {
        ui.mostrarTurno(atacante.getNombre());
        ui.mostrarEstado(defensor);

        if (atacante instanceof Mago) {
            ((Mago) atacante).lanzarHechizo(defensor);
        } else if (atacante instanceof Guerrero) {
            ((Guerrero) atacante).golpeFuerte(defensor);
        } else {
            atacante.atacar(defensor);
        }
    }

    /** Retorna true si alguno de los dos personajes ya no está vivo. */
    public boolean hayGanador(Personaje p1, Personaje p2) {
        return !p1.estaVivo() || !p2.estaVivo();
    }

    /** Devuelve el personaje que sigue vivo (el ganador). */
    public Personaje obtenerGanador(Personaje p1, Personaje p2) {
        return p1.estaVivo() ? p1 : p2;
    }
}

// =============================================================
// SUBSISTEMA 3: CombatManager
// Orquesta el ciclo completo de la pelea entre dos personajes.
// =============================================================
class CombatManager {
    private TurnManager turnManager;
    private UIManager ui;

    public CombatManager(TurnManager turnManager, UIManager ui) {
        this.turnManager = turnManager;
        this.ui = ui;
    }

    /**
     * Inicia la pelea en bucle de turnos alternados hasta que
     * uno de los personajes pierda todos sus puntos de vida.
     */
    public void iniciarPelea(Personaje p1, Personaje p2) {
        ui.mostrarInicio(p1.getNombre(), p2.getNombre());

        while (!turnManager.hayGanador(p1, p2)) {
            turnManager.ejecutarTurno(p1, p2);
            if (!turnManager.hayGanador(p1, p2)) {
                turnManager.ejecutarTurno(p2, p1);
            }
        }

        ui.mostrarGanador(turnManager.obtenerGanador(p1, p2));
    }
}

// =============================================================
// FACADE: JuegoLuchaFacade
// Punto de entrada único que oculta la complejidad de los
// subsistemas (UIManager, TurnManager, CombatManager).
// El cliente solo interactúa con esta clase.
// =============================================================
class JuegoLuchaFacade {
    private UIManager uiManager;
    private TurnManager turnManager;
    private CombatManager combatManager;

    public JuegoLuchaFacade() {
        // La fachada construye y conecta todos los subsistemas internamente.
        this.uiManager = new UIManager();
        this.turnManager = new TurnManager(uiManager);
        this.combatManager = new CombatManager(turnManager, uiManager);
    }

    /**
     * Método principal de la fachada.
     * Pide nombres, crea los personajes y lanza la pelea.
     * El cliente no necesita saber nada más allá de este método.
     */
    public void iniciarJuego() {
        String[] nombres = uiManager.pedirNombres();

        // Se crean personajes con tipos concretos (herencia en acción)
        Guerrero jugador1 = new Guerrero(nombres[0]);
        Mago jugador2 = new Mago(nombres[1]);

        combatManager.iniciarPelea(jugador1, jugador2);
    }
}

// =============================================================
// CLASE PRINCIPAL (Cliente)
// Solo conoce la fachada; ignora todo subsistema interno.
// =============================================================
class Main {
    public static void main(String[] args) {
        JuegoLuchaFacade juego = new JuegoLuchaFacade();
        juego.iniciarJuego();
    }
}