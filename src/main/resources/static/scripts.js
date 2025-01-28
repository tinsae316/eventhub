// Login Form
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    const response = await fetch('http://localhost:8080/api/users/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    });

    const data = await response.text();
    if (response.ok) {
        if (data === "Admin") {
            window.location.href = 'admin.html';
        } else {
            window.location.href = 'events.html';
        }
    } else {
        alert('Login failed: ' + data);
    }
});

// Admin Login Form
document.getElementById('adminLoginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('adminUsername').value;
    const password = document.getElementById('adminPassword').value;

    const response = await fetch('http://localhost:8080/api/users/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    });

    const data = await response.text();
    if (response.ok && data === "Admin") {
        window.location.href = 'admin.html';
    } else {
        alert('Admin login failed: ' + data);
    }
});

// Signup Form
document.getElementById('signupForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const age = document.getElementById('age').value;

    if (password !== confirmPassword) {
        alert('Passwords do not match!');
        return;
    }

    const response = await fetch('http://localhost:8080/api/users/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, age })
    });

    const data = await response.text();
    if (response.ok) {
        alert('Signup successful!');
        window.location.href = 'index.html';
    } else {
        alert('Signup failed: ' + data);
    }
});

// Fetch Events
async function fetchEvents(userAge) {
    const response = await fetch(`http://localhost:8080/api/events?userAge=${userAge}`);
    const events = await response.json();
    const container = document.getElementById('events-container');
    container.innerHTML = ''; // Clear previous content
    events.forEach(event => {
        const card = document.createElement('div');
        card.className = 'event-card';
        card.innerHTML = `
            <img src="${event.photoUrl}" alt="${event.name}">
            <h3>${event.name}</h3>
            <p>${event.description}</p>
            <p>Age Group: ${event.ageGroup}</p>
            <p>Capacity: ${event.registeredUsers}/${event.capacity}</p>
            <button onclick="registerForEvent(${event.id})">Register</button>
        `;
        container.appendChild(card);
    });
}

// Register for Event
async function registerForEvent(eventId) {
    const userId = 1; // Replace with actual user ID (e.g., from login)
    const response = await fetch(`http://localhost:8080/api/events/register?userId=${userId}&eventId=${eventId}`, {
        method: 'POST'
    });

    const data = await response.text();
    if (response.ok) {
        alert('Registration successful!');
        fetchEvents(20); // Refresh events
    } else {
        alert('Registration failed: ' + data);
    }
}

// Admin: Add Event
document.getElementById('addEventForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const event = {
        name: document.getElementById('eventName').value,
        description: document.getElementById('eventDescription').value,
        ageGroup: document.getElementById('eventAgeGroup').value,
        photoUrl: document.getElementById('eventPhotoUrl').value,
        capacity: document.getElementById('eventCapacity').value
    };

    const response = await fetch('http://localhost:8080/api/events/admin/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(event)
    });

    const data = await response.text();
    if (response.ok) {
        alert('Event added successfully!');
        fetchEvents(20); // Refresh events
    } else {
        alert('Failed to add event: ' + data);
    }
});

// Admin: Fetch Events
async function fetchAdminEvents() {
    const response = await fetch('http://localhost:8080/api/events');
    const events = await response.json();
    const container = document.getElementById('events-container');
    container.innerHTML = ''; // Clear previous content
    events.forEach(event => {
        const card = document.createElement('div');
        card.className = 'event-card';
        card.innerHTML = `
            <img src="${event.photoUrl}" alt="${event.name}">
            <h3>${event.name}</h3>
            <p>${event.description}</p>
            <p>Age Group: ${event.ageGroup}</p>
            <p>Capacity: ${event.registeredUsers}/${event.capacity}</p>
            <button onclick="deleteEvent(${event.id})">Delete</button>
        `;
        container.appendChild(card);
    });
}

// Admin: Delete Event
async function deleteEvent(eventId) {
    const response = await fetch(`http://localhost:8080/api/events/admin/delete/${eventId}`, {
        method: 'DELETE'
    });

    const data = await response.text();
    if (response.ok) {
        alert('Event deleted successfully!');
        fetchAdminEvents(); // Refresh events