package ma.emsi.mehdikheiri.tp0_kheiri_mehdi;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Backing bean pour la page JSF index.xhtml.
 * Portée view pour conserver l'état de la conversation pendant plusieurs requêtes HTTP.
 */
@Named
@ViewScoped
public class Bb implements Serializable {

    private String systemRole;
    private boolean systemRoleChangeable = true;
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    @Inject
    private FacesContext facesContext;

    public Bb() {
    }

    public String getSystemRole() {
        return systemRole;
    }

    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
    }

    public boolean isSystemRoleChangeable() {
        return systemRoleChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    /**
     * Envoie la question au serveur.
     * Analyse le texte de la question pour produire des statistiques détaillées.
     *
     * @return null pour rester sur la même page.
     */
    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        if (this.conversation.isEmpty()) {
            this.systemRoleChangeable = false;
        }

        this.reponse = analyzeText(question);
        afficherConversation();
        return null;
    }

    /**
     * Analyse les statistiques du texte fourni.
     *
     * @param input Texte saisi par l'utilisateur.
     * @return Statistiques formatées du texte.
     */
    private String analyzeText(String input) {
        int wordCount = input.split("\\s+").length;
        int charCountWithSpaces = input.length();
        int charCountWithoutSpaces = input.replace(" ", "").length();
        Map<Character, Integer> letterFrequency = new HashMap<>();

        for (char c : input.toLowerCase(Locale.FRENCH).toCharArray()) {
            if (Character.isLetter(c)) {
                letterFrequency.put(c, letterFrequency.getOrDefault(c, 0) + 1);
            }
        }

        StringBuilder frequencyString = new StringBuilder("Fréquence des lettres :\n");
        for (Map.Entry<Character, Integer> entry : letterFrequency.entrySet()) {
            frequencyString.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }

        return String.format("""
                Statistiques du texte :
                - Nombre de mots : %d
                - Nombre de caractères (avec espaces) : %d
                - Nombre de caractères (sans espaces) : %d
                %s
                """, wordCount, charCountWithSpaces, charCountWithoutSpaces, frequencyString);
    }

    /**
     * Réinitialise le chat en redirigeant vers la page index.xhtml.
     *
     * @return "index" pour changer de vue.
     */
    public String nouveauChat() {
        return "index";
    }

    /**
     * Ajoute la conversation actuelle à l'historique.
     */
    private void afficherConversation() {
        this.conversation.append("== User:\n").append(question).append("\n== Serveur:\n").append(reponse).append("\n");
    }

    /**
     * Retourne les rôles disponibles pour le système.
     *
     * @return Liste des rôles sous forme de SelectItem.
     */
    public List<SelectItem> getSystemRoles() {
        List<SelectItem> listeSystemRoles = new ArrayList<>();
        String role = """
                You are a helpful assistant. You help the user to find the information they need.
                If the user type a question, you answer it.
                """;
        listeSystemRoles.add(new SelectItem(role, "Assistant"));

        role = """
                You are an interpreter. You translate from English to French and from French to English.
                If the user type a French text, you translate it into English.
                If the user type an English text, you translate it into French.
                If the text contains only one to three words, give some examples of usage of these words in English.
                """;
        listeSystemRoles.add(new SelectItem(role, "Traducteur Anglais-Français"));

        role = """
                Your are a travel guide. If the user type the name of a country or of a town,
                you tell them what are the main places to visit in the country or the town
                are you tell them the average price of a meal.
                """;
        listeSystemRoles.add(new SelectItem(role, "Guide touristique"));

        this.systemRole = (String) listeSystemRoles.get(0).getValue();
        return listeSystemRoles;
    }
}
