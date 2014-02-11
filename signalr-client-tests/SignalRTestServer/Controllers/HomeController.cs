using Microsoft.AspNet.SignalR;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;

namespace SignalRTestServer.Controllers
{
    public class HomeController : Controller
    {
        //
        // GET: /Home/

        public ActionResult Index()
        {
            return Redirect("/index.html");
        }

        public EmptyResult TriggerTestMessage()
        {
            GlobalHost.ConnectionManager.GetConnectionContext<TestEndpoint>().Connection.Broadcast("test message");
            return new EmptyResult();
        }

        public EmptyResult TriggerHubTestMessage()
        {
            GlobalHost.ConnectionManager.GetHubContext<TestHub>().Clients.All.testMessage("dummyValue");
            return new EmptyResult();
        }

        public ContentResult LastSentData()
        {
            return Content(TestEndpoint.LastSentData);
        }

        public ContentResult LastHubData()
        {
            return Content(TestHub.LastSentData);
        }

    }
}
