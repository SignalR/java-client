using System;
using System.Web;
using Microsoft.AspNet.SignalR;
namespace SignalRTestServer
{
    public class IntegrationTestsHub : Hub
    {
        public void Echo(String data)
        {
            Clients.Caller.Echo(data);
        }

        public void UpdateState(String name, int val)
        {
            Clients.Caller.name = val;
        }

        public void TriggerError()
        {
            throw new HubException("Dummy error");
        }

        public void JoinGroup(String groupName)
        {
            this.Groups.Add(Context.ConnectionId, groupName);
        }

        public void LeaveGroup(String groupName)
        {
            this.Groups.Remove(Context.ConnectionId, groupName);
        }

        public void SendMessageToGroup(String groupName, String message)
        {
            this.Clients.Group(groupName).echo(message);
        }
    }
}