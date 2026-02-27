<%@ page import="tn.esprit.entities.User" %>
<%@ page import="java.util.List" %>

<%
    List<User> usersList = (List<User>) request.getAttribute("usersList");
    if(usersList == null) usersList = new java.util.ArrayList<>();
%>

<h2>Liste des Users</h2>
<table border="1">
    <tr>
        <th>ID</th><th>Nom</th><th>Prénom</th><th>Email</th><th>Adresse</th><th>Téléphone</th><th>Profil</th>
    </tr>
    <% for(User u : usersList) { %>
    <tr>
        <td><%= u.getId() %></td>
        <td><%= u.getNom() %></td>
        <td><%= u.getPrenom() %></td>
        <td><%= u.getEmail() %></td>
        <td><%= u.getAdresse() %></td>
        <td><%= u.getTelephone() %></td>
        <td><%= u.getProfil() != null ? u.getProfil().getNom() : "" %></td>
    </tr>
    <% } %>
</table>

<a href="index.jsp">Retour au menu</a>
