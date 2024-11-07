function sendMessage() {
    const userInput = document.getElementById('user-input').value;
    if (userInput.trim() === '') return;

    // Append user message to chat
    const chatMessages = document.getElementById('chat-messages');
    const userMessageDiv = document.createElement('div');
    userMessageDiv.className = 'chat-message user-message';
    userMessageDiv.textContent = userInput;
    chatMessages.appendChild(userMessageDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;

    // Clear input field
    document.getElementById('user-input').value = '';

    // Send message to server
    fetch('/chat', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ message: userInput })
    })
        .then(response => response.text())
        .then(data => {
            // Append bot response to chat
            const botMessageDiv = document.createElement('div');
            botMessageDiv.className = 'chat-message bot-message';
            botMessageDiv.textContent = data;
            chatMessages.appendChild(botMessageDiv);
            chatMessages.scrollTop = chatMessages.scrollHeight;
        })
        .catch(error => {
            console.error('Error:', error);
        });
}