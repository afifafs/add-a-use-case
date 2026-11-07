const student = {
  id: 4,
  username: 'j.smith@abc.edu',
  firstName: 'John',
  lastName: 'Smith',
  email: 'j.smith@abc.edu',
  enabled: true,
  sectionId: 2,
  sectionName: '2023-2024',
  teamId: 1,
  teamName: 'Team1'
}

const searchResponse = (enabled) => ({
  flag: true,
  code: 200,
  message: 'Find students successfully',
  data: {
    content: [{ ...student, enabled }],
    pageable: { pageNumber: 0, pageSize: 50 },
    totalElements: 1,
    totalPages: 1,
    last: true,
    size: 50,
    number: 0,
    first: true,
    numberOfElements: 1,
    empty: false
  }
})

describe('Student deactivation', () => {
  beforeEach(() => {
    cy.login('b.gates@abc.edu', '123456')
  })

  it('explains the consequences and leaves the account unchanged when canceled (extension 4a)', () => {
    cy.intercept('POST', '**/students/search', { body: searchResponse(true) }).as('students')
    cy.intercept('POST', '**/students/4/deactivate').as('deactivate')
    cy.visit('/students')
    cy.wait('@students')

    cy.get('[data-cy=deactivate-student-4]').click()
    cy.get('[data-cy=deactivation-dialog]').should('be.visible')
    cy.contains('stop her from logging in and submitting work')
    cy.contains('stay on her team and remain in past reports and evaluations')
    cy.screenshot('student-deactivation-confirmation')

    cy.get('[data-cy=deactivation-cancel]').click()

    cy.get('[data-cy=deactivation-dialog]').should('not.be.visible')
    cy.get('@deactivate.all').should('have.length', 0)
  })

  it('confirms deactivation, calls the dedicated endpoint, and refreshes the row', () => {
    let searchCount = 0
    cy.intercept('POST', '**/students/search', (request) => {
      searchCount += 1
      request.reply({ body: searchResponse(searchCount === 1) })
    }).as('students')
    cy.intercept('POST', '**/students/4/deactivate', {
      body: {
        flag: true,
        code: 200,
        message: 'Deactivate student successfully',
        data: { ...student, enabled: false }
      }
    }).as('deactivate')

    cy.visit('/students')
    cy.wait('@students')
    cy.get('[data-cy=deactivate-student-4]').click()
    cy.get('[data-cy=deactivation-confirm]').click()

    cy.wait('@deactivate').its('request.method').should('eq', 'POST')
    cy.wait('@students')
    cy.get('[data-cy=deactivate-student-4]').should('not.exist')
    cy.contains('Disabled')
  })

  it('does not allow deactivation through the edit-form status control', () => {
    cy.intercept('POST', '**/students/search', { body: searchResponse(true) }).as('students')
    cy.intercept('GET', '**/students/4', {
      body: { flag: true, code: 200, message: 'Find student successfully', data: student }
    }).as('student')

    cy.visit('/students')
    cy.wait('@students')
    cy.get('[data-cy=edit-student-4]').click()
    cy.wait('@student')

    cy.get('.el-switch').should('have.class', 'is-disabled')
  })

  it('keeps the edit-form status control for reactivating an inactive student', () => {
    cy.intercept('POST', '**/students/search', { body: searchResponse(false) }).as('students')
    cy.intercept('GET', '**/students/4', {
      body: { flag: true, code: 200, message: 'Find student successfully', data: { ...student, enabled: false } }
    }).as('student')

    cy.visit('/students')
    cy.wait('@students')
    cy.get('[data-cy=edit-student-4]').click()
    cy.wait('@student')

    cy.get('.el-switch').should('not.have.class', 'is-disabled')
  })
})
