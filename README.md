# internet-relay-chat
IRC local desenvolvido em Java utilizando protocolo UDP. PUCRS/2019 - Sistemas Operacionais e Redes de Computadores

# Como usar
* Mude o host no arquivo Client.java para o IP Local da maquina que irá rodar o server.
* Execute o arquivo Server.java na maquina Servidor e depois execute o arquivo Client.java nas maquinas que irão se conectar.
* Ao se conectar, escolha seu nick.
* Pronto! digite /help para mais instruções!

# Comandos Globais - podem ser usados em qualquer lugar

  /help - Mostra os comandos disponíveis.
  
  /nick <nickname> - Solicita a alteração do apelido do usuário.
  
  /list - Solicita a lista de canais disponíveis no servidor.
  
  /join <channel> - Solicita a participação em um canal. Caso o canal não exista, e o usuário estejá no lobby, ele é criado automaticamente.
  
  /quit = Desconecta completamente do serviço de chat. 
  
# Comandos do Lobby - podem ser usados apenas do Lobby
  
  /create <channel> - Solicita a criação de um novo canal no servidor e garante a posição de Administrador.
  
# Comandos do Canal - podem ser usados apenas dentro de um canal
  
  /part - Solicita a saída do canal atual.
  
  /names - Solicita a lista de usuários que fazem parte do canal atual.
 
  /msg <nickname> <message> - Envia uma mensagem privada para um usuário.
  
# Comandos do Administrador - podem ser usados apenas pelo Administrador

  /remove - Solicita a remoção de um canal e todos os usuários nele conectados. Envia todo mundo que estava no canal para o Lobby
  
  /kick <nickname> - Solicita a remoção de um usuário de um canal.
