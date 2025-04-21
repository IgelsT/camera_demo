<?php

namespace App\Mail;

use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;
use Illuminate\Mail\Mailables\Address;

class SimpleHtmlMail extends Mailable
{
    use Queueable, SerializesModels;

    public function __construct(
        private string $subject_str,
        private string $message
    ) {}

    public function build()
    {
        return $this->html($this->message)
            ->from(env('MAIL_FROM_ADDRESS'), env('MAIL_FROM_NAME', 'Camera Mailer'))
            ->subject($this->subject_str);
    }

    // public function envelope(): Envelope
    // {
    //     return new Envelope(
    //         from: new Address(env('MAIL_FROM_ADDRESS'), 'Camera Mailer'),
    //         subject: 'Order Shipped',
    //     );
    // }

    // public function content(): Content
    // {
    //     return new Content(
    //         text: 'mail',
    //     );
    // }

    /**
     * Get the attachments for the message.
     *
     * @return array<int, \Illuminate\Mail\Mailables\Attachment>
     */
    // public function attachments(): array
    // {
    //     return [];
    // }
}
