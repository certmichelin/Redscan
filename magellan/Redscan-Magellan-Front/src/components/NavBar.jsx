import { Container, Nav, Navbar, NavDropdown } from 'react-bootstrap';
import './NavBar.css';

function NavBar() {
    return (
      <Navbar expand="lg" sticky="top" className="bg-body-white border-bottom border-light-subtle py-4 mb-5">
        <Container fluid className='mx-auto w-75 px-0'>
          <Navbar.Brand className='fs-1 fw-medium me-5' id='navbar-brand'>MAGELLAN</Navbar.Brand>
          <Navbar.Toggle aria-controls="basic-navbar-nav" />
          <Navbar.Collapse id="basic-navbar-nav" className='d-flex flex-row-reverse'>
            <Nav className=''>
              <Nav.Link href="/magellan/brands" className='text-center mx-3'>Brands</Nav.Link>
              <Nav.Link href="/magellan/masterdomains" className='text-center mx-3'>Masterdomains</Nav.Link>
              <Nav.Link href="/magellan/ipranges" className='text-center mx-3'>IP ranges</Nav.Link>
              <NavDropdown title="Other items" id="basic-nav-dropdown" className='text-center mx-3'>
                <NavDropdown.Item href="/magellan/domains" className='text-secondary'>Domains</NavDropdown.Item>
                <NavDropdown.Item href="/magellan/ips" className='text-secondary'>Ips</NavDropdown.Item>
              </NavDropdown>
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>
    );
  }
  
  export default NavBar;