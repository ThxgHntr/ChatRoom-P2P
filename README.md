# Chat Program Readme

## Overview

This is a simple Java Swing-based chat program that allows clients to communicate over a multicast network. The program consists of a GUI application that facilitates real-time messaging between clients. Each client is identified by their IP address and port number.

## Prerequisites

- Java Development Kit (JDK) installed on your machine.
- Basic understanding of Java programming.

## Usage

Upon running the program, a GUI window will appear, allowing you to send and receive messages. The interface is divided into three main sections:

1. **Chat Panel**: Displays the conversation between you and the selected opponent.

2. **Clients Panel**: Lists all connected clients. Click on a client to select them as the messaging partner.

3. **Message Input**: Type your message in the text field at the bottom. Press `Enter` or click the "Send" button to send the message.

## Features

- **Multicast Communication**: Clients communicate over a multicast group (address: 239.0.0.1, port: 1111).

- **Real-time Updates**: The program notifies clients when a new user joins or leaves the chat. The clients panel is dynamically updated.

- **Private Messaging**: You can select a specific client to send private messages.

- **Leave Chat**: When closing the application, the program sends a leave packet to notify others about your departure.

## Implementation Details

- The program uses Java Swing for the graphical user interface.
  
- Communication is implemented using DatagramSocket and MulticastSocket for sending and receiving messages.

- Each client is represented by the `ClientModel` class, and a list of clients is managed by the `ClientModelList` class.

- Real-time updates and communication between clients are achieved through multicast messages.

## Notes

- The program assumes that clients are on the same multicast group and have network connectivity.

- Make sure that the multicast group address (239.0.0.1) and port (1111) are not blocked by firewalls.

- The program uses Java records for a concise representation of client information.

## Contributing

Feel free to contribute by submitting issues or pull requests.
