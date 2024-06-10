import React from 'react';
import { Form, Container, Row, Col } from 'react-bootstrap';
import './NumberEntriesSelection.css';

function NumberEntriesSelection({ changeNumberEntries }) {
    return (
      <Container>
        <Row>
          <Col  md="auto" className='d-flex align-items-center justify-content-end pe-1'>
            <p className='number-entries-selection text-secondary text-end m-0'>Results by page: </p>
          </Col>

          <Col className='d-flex align-items-center justify-content-start ps-1'>
            <Form.Select onChange={(e) => changeNumberEntries(e.target.value)} size='sm' className='number-entries-selection text-start w-auto text-secondary border-0 bg-light'>
              <option value="10">10</option>
              <option value="25">25</option>
              <option value="50">50</option>
            </Form.Select>
          </Col>
        </Row>
      </Container>     
    );
  }
  
  export default NumberEntriesSelection;