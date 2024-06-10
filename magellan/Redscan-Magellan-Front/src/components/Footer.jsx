import { Container, Row, Col } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faGithub } from '@fortawesome/free-brands-svg-icons'
import './Footer.css';

function Footer() {
    const currentYear = new Date().getFullYear();

    return (
      <Container className='bg-light py-5'>
        <Row className='w-75 mx-auto'>
          <Col className='p-0'>
            <Row className='fw-light'>
              <p className='text-secondary my-1 ps-0'>© {currentYear} Copyright: <a className='text-secondary text-decoration-none fst-italic' href="https://cert.michelin.com/" target="_blank" rel="noreferrer">CERT Michelin</a></p>
            </Row>
            <Row className='fw-light'>
              <p className='text-secondary my-1 ps-0'>Licensed under the Apache License, Version 2.0</p>
            </Row>
          </Col>
          <Col className='d-flex align-items-center justify-content-end p-0'>
            <a href='https://github.com/certmichelin' target="_blank" rel="noreferrer" className='link-opacity-50-hover link-secondary'><FontAwesomeIcon icon={faGithub} size="2xl" /></a>
          </Col>
        </Row>
      </Container>

    );
  }
  
  export default Footer;