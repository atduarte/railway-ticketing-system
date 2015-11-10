<?php

namespace AppBundle\Controller;

use AppBundle\Document\Ticket;
use Symfony\Component\Form\Exception\InvalidArgumentException;
use Symfony\Component\HttpFoundation\Request;
use FOS\RestBundle\Controller\Annotations\Get;
use FOS\RestBundle\Controller\Annotations\Post;
use Symfony\Component\Routing\Exception\InvalidParameterException;
use Symfony\Component\Validator\Constraints\DateTime;

class TicketsController extends BaseController
{
    /**
     * @Get("/stations")
     * @param Request $request
     * @return array
     */
    public function getStationsAction(Request $request)
    {
        $this->requireUserRole($request);

        $lines = $this->get('train_information')->getLines();
        $stations = [];

        foreach ($lines as $line) {
            $stations = array_merge($stations, array_column($line['stations'], 'name'));
        }

        $stations = array_values(array_unique($stations));
        return ['stations' => $stations];
    }

    /**
     * @Get("/tickets")
     * @param Request $request
     * @return \Symfony\Component\HttpFoundation\Response
     */
    public function getAvailableTicketsAction(Request $request)
    {
        $this->requireUserRole($request);

        $from = $request->query->get('from');
        $to = $request->query->get('to');
        $date = $this->parseDate($request->query->get('date'));

        return $this->get('train_manager')->getDailyTrips($from, $to, $date);
    }

    /**
     * @Post("/ticket")
     * @param Request $request
     * @return array
     * @throws \Exception
     */
    public function buyTicketAction(Request $request)
    {
        $this->requireUserRole($request);

        $user = $this->user;
        $lineNumber = $request->request->get('lineNumber');
        $lineDeparture = $request->request->get('lineDeparture');
        $from = $request->request->get('from');
        $to = $request->request->get('to');
        $date = $this->parseDate($request->request->get('date'));

        $ticket = $this->get('train_manager')->buyTicket($user, $date, $lineNumber, $from, $to, $lineDeparture);

        if (!$ticket) {
            throw new \Exception('Couldn\'t buy the ticket');
        }

        return $ticket->toArray();
    }

    /**
     * @Get("/my-tickets")
     * @param Request $request
     * @return array
     */
    public function getBoughtTicketsAction(Request $request)
    {
        $this->requireUserRole($request);

        return array_map(
            function ($ticket) { return $ticket->toArray(); },
            $this->get('doctrine.odm.mongodb.document_manager')->getRepository('AppBundle:Ticket')
                ->findBy(['user.$id' => new \MongoId($this->user->getId())])
        );
    }

    /**
     * @Get("/lines")
     * @param Request $request
     * @return array
     */
    public function getLinesAction(Request $request)
    {
        // $this->requireInspectorRole($request);

        $lines = $this->get('train_information')->getLines();

        return array_map(function ($line) {
            return [
                'lineNumber' => $line['number'],
                'from' => $line['stations'][0]['name'],
                'to' => end($line['stations'])['name'],
                'stations' => $line['stations'],
                'duration' => $line['duration'],
                'departures' => $line['departures']
            ];
        }, $lines);

    }

//    /**
//     * @param Request $request
//     * @return array
//     */
//    public function getTicketsForValidationAction(Request $request)
//    {
//        $this->requireInspectorRole($request);
//
//        $lineNumber = $request->query->get('line');
//        $departure = $request->query->get('departure');
//
//        if (!is_numeric($lineNumber) || !is_numeric($departure)) {
//            throw new InvalidArgumentException('Line Number ("line") and Departure ("departure") query parameters are required and should be numeric.');
//        }
//
//        // Get Trip
//        $date = new \DateTime();
//        $date->setTime(0,0);
//        $trip = $this->get('doctrine.odm.mongodb.document_manager')->getRepository('AppBundle:Trip')
//            ->findOneBy(['date' => new \MongoDate($date->getTimestamp())]);
//
//        // Get Tickets(
//        $tickets = [];
//        if ($trip) {
//            $tickets = $this->get('doctrine.odm.mongodb.document_manager')->getRepository('AppBundle:Ticket')
//                ->findBy(['trip.$id' => new \MongoId($trip->getId())]);
//        }
//
//        return array_map(function (Ticket $ticket) { return $ticket->toArray(); }, $tickets);
//    }

    protected function parseDate($date)
    {
        $date = \DateTime::createFromFormat('Y-m-d', $date);
        $errors = \DateTime::getLastErrors();

        if ($errors['error_count'] + $errors['warning_count'] > 0) {
            throw new \RuntimeException('Invalid date');
        }

        $date->setTime(0,0,0);
        return $date;
    }
}
